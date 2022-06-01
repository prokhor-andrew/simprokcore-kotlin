//
//  StateAction.kt
//  simprokcore
//
//  Created by Andrey Prokhorenko on 12.03.2022.
//  Copyright (c) 2022 simprok. All rights reserved.

package com.simprok.simprokcore

/**
 * This class is public for implementation reasons only. It should not be used directly.
 */
sealed interface StateAction<State, Event> {

    data class WillUpdate<State, Event>(val event: Event) : StateAction<State, Event>

    data class DidUpdate<State, Event>(val state: State) : StateAction<State, Event>
}