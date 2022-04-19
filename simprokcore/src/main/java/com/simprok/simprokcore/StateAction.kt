//
//  StateAction.kt
//  simprokcore
//
//  Created by Andrey Prokhorenko on 12.03.2022.
//  Copyright (c) 2022 simprok. All rights reserved.

package com.simprok.simprokcore

import com.simprok.simprokmachine.api.Mapper

/**
 * This class is public for implementation reasons only. It should not be used directly.
 */
sealed interface StateAction<State> {

    data class WillUpdate<State>(
        val mapper: Mapper<State?, ReducerResult<State>>
    ) : StateAction<State>

    data class DidUpdate<State>(val state: State) : StateAction<State>
}