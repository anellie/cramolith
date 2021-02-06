/*
 * Developed as part of the Cramolith project.
 * This file was last modified at 2/6/21, 2:11 AM.
 * Copyright 2021, see git repository at git.angm.xyz for authors and other info.
 * This file is under the GPL3 license. See LICENSE in the root directory of this repository for details.
 */

package xyz.angm.cramolith.common.pokemon

import com.badlogic.gdx.utils.IntMap
import com.badlogic.gdx.utils.ObjectMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import ktx.assets.file
import xyz.angm.cramolith.client.resources.I18N
import xyz.angm.cramolith.common.yaml

class Species(
    val ident: String,
    val type: Type,
    val moves: IntMap<Move>,
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val speed: Int
) {

    val name = I18N.tryGet(ident) ?: ident.capitalize()

    companion object {

        private val species = ObjectMap<String, Species>()

        init {
            val raw = yaml.decodeFromString(MapSerializer(String.serializer(), Serialized.serializer()), file("species.yaml").toString())
            for (s in raw) {
                species.put(s.key, s.value.into(s.key))
            }
        }

        @Serializable
        private class Serialized(
            val type: Type,
            val moves: HashMap<Int, String>,
            val hp: Int,
            val attack: Int,
            val defense: Int,
            val speed: Int
        ) {
            fun into(ident: String): Species {
                val moves = IntMap<Move>(moves.size)
                for (move in this.moves) {
                    moves.put(move.key, Move.of(move.value))
                }
                return Species(ident, type, moves, hp, attack, defense, speed)
            }
        }
    }
}