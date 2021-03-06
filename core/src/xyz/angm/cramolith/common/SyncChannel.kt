/*
 * Developed as part of the Cramolith project.
 * This file was last modified at 2/4/21, 12:43 PM.
 * Copyright 2021, see git repository at git.angm.xyz for authors and other info.
 * This file is under the GPL3 license. See LICENSE in the root directory of this repository for details.
 */

package xyz.angm.cramolith.common

class SyncChannel<T>(private val receiver: T) {

    @Synchronized
    operator fun invoke(fn: T.() -> Unit) {
        fn(receiver)
    }
}