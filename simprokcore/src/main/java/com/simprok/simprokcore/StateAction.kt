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
sealed interface StateAction<Event, State> {

    data class WillUpdate<Event, State>(val event: Event) : StateAction<Event, State>

    data class DidUpdate<Event, State>(val state: State) : StateAction<Event, State>
}