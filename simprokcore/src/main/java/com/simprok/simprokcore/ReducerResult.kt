//
//  ReducerResult.kt
//  simprokcore
//
//  Created by Andrey Prokhorenko on 12.03.2022.
//  Copyright (c) 2022 simprok. All rights reserved.

package com.simprok.simprokcore

/**
 * A type that represents a behavior of a layer's reducer.
 */
sealed interface ReducerResult<T> {

    /**
     * Returning this value from layer's `reducer` method ensures
     * that the state *will* be changed and emitted.
     */
    class Set<T>(val value: T) : ReducerResult<T>

    /**
     *  Returning this value from layer's `reducer` method ensures
     *  that the state *won't* be changed and emitted .
     */
    class Skip<T> : ReducerResult<T>
}