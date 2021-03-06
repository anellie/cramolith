/*
 * Developed as part of the Cramolith project.
 * This file was last modified at 5/6/21, 7:16 PM.
 * Copyright 2021, see git repository at git.angm.xyz for authors and other info.
 * This file is under the GPL3 license. See LICENSE in the root directory of this repository for details.
 */

package xyz.angm.cramolith.client.graphics.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.kotcrab.vis.ui.util.dialog.Dialogs
import com.kotcrab.vis.ui.widget.VisWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import ktx.actors.plusAssign
import xyz.angm.cramolith.client.Cramolith
import xyz.angm.cramolith.client.actions.PlayerInputHandler
import xyz.angm.cramolith.client.ecs.PlayerMapper
import xyz.angm.cramolith.client.ecs.systems.RenderSystem
import xyz.angm.cramolith.client.ecs.systems.TriggerSystem
import xyz.angm.cramolith.client.graphics.panels.menu.MessagePanel
import xyz.angm.cramolith.client.graphics.windows.BattleWindow
import xyz.angm.cramolith.client.graphics.windows.ChatWindow
import xyz.angm.cramolith.client.graphics.windows.MenuWindow
import xyz.angm.cramolith.client.networking.Client
import xyz.angm.cramolith.client.resources.I18N
import xyz.angm.cramolith.client.world.Script
import xyz.angm.cramolith.client.world.World
import xyz.angm.cramolith.common.ecs.components.IgnoreSyncFlag
import xyz.angm.cramolith.common.ecs.components.specific.BattleComponent
import xyz.angm.cramolith.common.ecs.network
import xyz.angm.cramolith.common.ecs.playerM
import xyz.angm.cramolith.common.ecs.systems.NetworkSystem
import xyz.angm.cramolith.common.ecs.systems.RemoveSystem
import xyz.angm.cramolith.common.ecs.systems.VelocitySystem
import xyz.angm.cramolith.common.networking.*
import xyz.angm.cramolith.common.pokemon.battle.Battle
import xyz.angm.cramolith.common.pokemon.battle.Opponent
import xyz.angm.cramolith.common.pokemon.battle.PlayerOpponent
import xyz.angm.cramolith.common.pokemon.battle.PokeBattleState
import xyz.angm.cramolith.common.runLogE
import xyz.angm.rox.Engine
import xyz.angm.rox.Entity
import xyz.angm.rox.EntityListener
import xyz.angm.rox.Family.Companion.allOf
import xyz.angm.rox.systems.EntitySystem

/** The game screen. Active during gameplay.
 *
 * This screen is mainly a bag of other objects that make up game state;
 * and should not have any other responsibility other than initializing them and
 * setting up their interactions that drive the game.
 * The only other responsibility of this class is putting together all graphics sources and drawing them.
 *
 * The screen is initialized by [Cramolith], which means that it's only created after a server connection
 * was established and the initial [InitPacket] was received.
 *
 * @param client The client for communicating with the server
 *
 * @property engine The ECS engine used
 * @property player The player controlled by this game instance */
class GameScreen(
    private val game: Cramolith,
    val client: Client,
    val player: Entity,
    entities: Array<Entity>,
    messages: Array<GlobalChatMsg>
) : Screen() {

    private val coScope = CoroutineScope(Dispatchers.Default)

    // Entities
    val engine = Engine()
    private val netSystem = NetworkSystem(client::send, player[playerM].clientUUID)
    private val inputHandler = PlayerInputHandler(this)
    val players = PlayerMapper()
    private val playersFamily = allOf(playerM)

    // 2D Graphics
    val world = World(this)
    private val activeWindows = HashMap<String, VisWindow>()
    var battleWindow: BattleWindow? = null

    val entitiesLoaded get() = engine.entities.size
    val systemsActive get() = engine.systems.size
    val onlinePlayers get() = engine[playersFamily]

    init {
        initSystems()
        engine.add(player)
        entities.forEach { if (it[network].id != player[network].id) engine.add(it) }

        initRender()
        initState(messages)
    }

    override fun render(delta: Float) {
        runLogE("Client", "rendering") { renderInternal(delta) }
    }

    private var tick = 0

    private fun renderInternal(delta: Float) {
        client.lock()
        Cramolith.execRunnables()
        engine.update(delta)
        stage.act()
        tick++
        if (tick > 5) {
            player[network].needsSync = true
            tick = 0
        }
        client.unlock()

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        stage.draw()
    }

    fun initBattle(opponent: Opponent, message: String, showPopup: Boolean = true, onComplete: () -> Unit) {
        val c = BattleComponent()
        c.battle = Battle(PlayerOpponent(player[playerM].clientUUID), opponent)
        player.add(engine, c)

        for (mon in player[playerM].pokemon) {
            mon.battleState = PokeBattleState(mon.hp)
        }

        battleWindow = BattleWindow(this, message) { won ->
            val text = if (won) I18N["battle.won"] else I18N["battle.lost"]
            if (showPopup) Dialogs.showOKDialog(stage, I18N["battle.over-title"], text)
            onComplete()
        }
        stage += battleWindow!!
    }

    fun setCutsceneStatus(inCut: Boolean) {
        player[playerM].isInCutscene = inCut
        player[network].needsSync = true
        inputHandler.disabled = inCut
    }

    fun loadScriptCutscene(triggerIdx: Int) {
        val playerC = player[playerM]
        if (playerC.isInCutscene) return
        val map = playerC.actorsTriggered.getOrPut(world.map.index, { HashSet() })
        if (map.contains(triggerIdx)) return

        setCutsceneStatus(true)
        val actor = world.map.actorsId[triggerIdx]
        Script(this, actor) { setCutsceneStatus(false) }
    }

    /** Toggles a window.
     * @param name The name of the window to uniquely identify its type
     * @param create A closure to create the window if it isn't active yet and will be added */
    fun toggleWindow(name: String, create: (GameScreen) -> VisWindow) {
        val window = activeWindows[name]
        when {
            window == null -> {
                val new = create(this)
                new.centerWindow()
                stage += new.fadeIn()
                activeWindows[name] = new
            }
            window.stage == stage -> window.fadeOut()
            else -> stage.addActor(window.fadeIn())
        }
    }

    /** Called when the game can no longer continue (disconnect; player quit; etc.)
     * Returns to the menu screen.
     * @param message The message to display. Defaults to no message which will return to menu screen immediately. */
    fun returnToMenu(message: String? = null) {
        client.send(player) // Make sure the player is up-to-date on the server
        client.disconnectListener = {} // Prevent it from showing the 'disconnected' message when it shouldn't
        game.screen = MenuScreen(game)
        dispose()
        (game.screen as MenuScreen).pushPanel(MessagePanel(game.screen as MenuScreen, message ?: return) {
            (game.screen as MenuScreen).popPanel()
        })
    }

    // Initialize all ECS systems
    private fun initSystems() = engine.apply {
        addLocalPlayerComponents()
        add(netSystem as EntitySystem)
        add(netSystem as EntityListener)
        client.addListener {
            when (it) {
                is Entity -> netSystem.receive(it)
                is BattleUpdatePacket -> battleWindow?.battleUpdate(it)

                is Array<*> -> {
                    it.forEach {
                        if (it !is Entity) return@forEach
                        if (it[network].id != player[network].id) netSystem.receive(it)
                    }
                }
            }
        }

        val renderSystem = RenderSystem(this@GameScreen)
        add(renderSystem as EntitySystem)
        add(renderSystem as EntityListener)

        add(VelocitySystem())
        add(TriggerSystem(this@GameScreen))
        add(RemoveSystem())
        add(players)
    }

    // Initialize everything not render-related
    private fun initState(messages: Array<GlobalChatMsg>) {
        // Windows
        toggleWindow("menu") { MenuWindow(it) }
        activeWindows["chat"] = ChatWindow(this, messages)

        // Network
        client.disconnectListener = { Cramolith.postRunnable { returnToMenu("disconnected-from-server") } }
        client.send(PrivateMessagePacket("[CYAN]${player[playerM].name}[LIGHT_GRAY] ${I18N["joined-game"]}"))

        // Input
        val multiplex = InputMultiplexer()
        multiplex.addProcessor(stage)
        multiplex.addProcessor(inputHandler)
        Gdx.input.inputProcessor = multiplex
    }

    // Adds local components to the player entity.
    private fun addLocalPlayerComponents() = player.apply {
        add(engine, IgnoreSyncFlag())
    }

    // Initialize all rendering components
    private fun initRender() {
        stage += world
    }

    override fun dispose() {
        super.dispose()
        client.close()
        coScope.cancel()
    }
}