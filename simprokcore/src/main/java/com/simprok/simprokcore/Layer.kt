//
//  Layer.kt
//  simprokcore
//
//  Created by Andrey Prokhorenko on 12.03.2022.
//  Copyright (c) 2022 simprok. All rights reserved.

package com.simprok.simprokcore

import com.simprok.simprokmachine.api.Mapper
import com.simprok.simprokmachine.api.Ward
import com.simprok.simprokmachine.api.inward
import com.simprok.simprokmachine.api.outward
import com.simprok.simprokmachine.machines.Machine

/**
 * A general interface that describes a type that represents a layer object.
 */
sealed interface Layer<Event> {

    val child: Machine<StateAction<Event>, StateAction<Event>>
}


interface MachineLayerType<Event, Input, Output> : Layer<Event> {

    override val child: Machine<StateAction<Event>, StateAction<Event>>
        get() = machine.outward<Input, Output, StateAction<Event>> {
            Ward.set(
                StateAction.WillUpdate(mapOutput(it))
            )
        }.inward {
            when (it) {
                is StateAction.WillUpdate<Event> -> Ward.set()
                is StateAction.DidUpdate<Event> -> Ward.set(
                    mapEvent(it.event)
                )
            }
        }

    val machine: Machine<Input, Output>

    fun mapEvent(event: Event): Input

    fun mapOutput(output: Output): Event
}

data class MachineLayerObject<Event, Input, Output>(
    override val machine: Machine<Input, Output>,
    private val stateMapper: Mapper<Event, Input>,
    private val eventMapper: Mapper<Output, Event>,
) : MachineLayerType<Event, Input, Output> {

    override fun mapEvent(event: Event): Input = stateMapper(event)

    override fun mapOutput(output: Output): Event = eventMapper(output)
}

interface ConsumerLayerType<Event, Input, Output> :
    Layer<Event> {

    override val child: Machine<StateAction<Event>, StateAction<Event>>
        get() = machine.outward<Input, Output, StateAction<Event>> {
            Ward.set()
        }.inward {
            when (it) {
                is StateAction.WillUpdate<Event> -> Ward.set()
                is StateAction.DidUpdate<Event> -> Ward.set(
                    map(it.event)
                )
            }
        }

    val machine: Machine<Input, Output>

    fun map(event: Event): Input
}

data class ConsumerLayerObject<Event, Input, Output>(
    override val machine: Machine<Input, Output>,
    private val mapper: Mapper<Event, Input>,
) : ConsumerLayerType<Event, Input, Output> {

    override fun map(event: Event): Input = mapper(event)
}


interface ProducerLayerType<Event, Input, Output> : Layer<Event> {

    override val child: Machine<StateAction<Event>, StateAction<Event>>
        get() = machine.outward<Input, Output, StateAction<Event>> {
            Ward.set(
                StateAction.WillUpdate(map(it))
            )
        }.inward {
            Ward.set()
        }

    val machine: Machine<Input, Output>

    fun map(output: Output): Event
}


data class ProducerLayerObject<Event, Input, Output>(
    override val machine: Machine<Input, Output>,
    private val mapper: Mapper<Output, Event>,
) : ProducerLayerType<Event, Input, Output> {

    override fun map(output: Output): Event = mapper(output)
}


interface MapEventLayerType<Event, Output> : Layer<Event> {

    override val child: Machine<StateAction<Event>, StateAction<Event>>
        get() = machine.outward<Event, Output, StateAction<Event>> {
            Ward.set(
                StateAction.WillUpdate(map(it))
            )
        }.inward {
            when (it) {
                is StateAction.WillUpdate<Event> -> Ward.set()
                is StateAction.DidUpdate<Event> -> Ward.set(it.event)
            }
        }

    val machine: Machine<Event, Output>

    fun map(output: Output): Event
}

data class MapEventLayerObject<Event, Output>(
    override val machine: Machine<Event, Output>,
    private val mapper: Mapper<Output, Event>,
) : MapEventLayerType<Event, Output> {

    override fun map(output: Output): Event = mapper(output)
}

interface MapStateLayerType<Event, Input> : Layer<Event> {

    override val child: Machine<StateAction<Event>, StateAction<Event>>
        get() = machine.outward<Input, Event, StateAction<Event>> {
            Ward.set(
                StateAction.WillUpdate(it)
            )
        }.inward {
            when (it) {
                is StateAction.WillUpdate<Event> -> Ward.set()
                is StateAction.DidUpdate<Event> -> Ward.set(
                    map(it.event)
                )
            }
        }

    val machine: Machine<Input, Event>

    fun map(event: Event): Input
}

data class MapStateLayerObject<Event, Input>(
    override val machine: Machine<Input, Event>,
    private val mapper: Mapper<Event, Input>,
) : MapStateLayerType<Event, Input> {

    override fun map(event: Event): Input = mapper(event)
}

interface NoMapLayerType<Event> : Layer<Event> {

    override val child: Machine<StateAction<Event>, StateAction<Event>>
        get() = machine.outward<Event, Event, StateAction<Event>> {
            Ward.set(
                StateAction.WillUpdate(it)
            )
        }.inward {
            when (it) {
                is StateAction.WillUpdate<Event> -> Ward.set()
                is StateAction.DidUpdate<Event> -> Ward.set(it.event)
            }
        }

    val machine: Machine<Event, Event>
}

data class NoMapLayerObject<Event>(
    override val machine: Machine<Event, Event>,
) : NoMapLayerType<Event>