/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.api.util.weighted;

import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackBuilder;
import org.spongepowered.api.util.VariableAmount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Represents an item stack with a range of possible quantities and a numerical
 * weight used for random selection from a collection of weighted types.
 */
public class WeightedItem extends WeightedObject<ItemType> implements DataSerializable {

    public static final DataQuery WEIGHTED_ITEM_TYPE = of("ItemType");
    public static final DataQuery WEIGHTED_ITEM_WEIGHT = of("Weight");
    public static final DataQuery WEIGHTED_ITEM_DATA = of("Data");
    public static final DataQuery WEIGHTED_ITEM_QUANTITY = of("Quantity");

    private ImmutableList<ImmutableDataManipulator<?, ?>> additionalProperties;
    private VariableAmount quantity;

    /**
     * Creates a new {@link WeightedEntity} with no additional properties.
     *
     * @param object The entity type
     * @param quantity The item quantity
     * @param weight The weight
     */
    public WeightedItem(ItemType object, VariableAmount quantity, int weight) {
        super(object, weight);
        this.additionalProperties = ImmutableList.of();
        this.quantity = quantity;
    }

    /**
     * Creates a new {@link WeightedEntity} with the given additional
     * properties.
     *
     * @param object The entity type
     * @param quantity The item quantity
     * @param weight The weight
     * @param collection The additional properties to apply to the entity
     */
    public WeightedItem(ItemType object, VariableAmount quantity, int weight, Collection<? extends DataManipulator<?, ?>> collection) {
        super(object, weight);
        ImmutableList.Builder<ImmutableDataManipulator<?, ?>> builder = ImmutableList.builder();
        for (DataManipulator<?, ?> property : collection) {
            builder.add(property.asImmutable());
        }
        this.additionalProperties = builder.build();
        this.quantity = quantity;
    }

    /**
     * Gets a {@link VariableAmount} representing the quantity of the item.
     *
     * @return The varible quantity
     */
    public VariableAmount getQuantity() {
        return this.quantity;
    }

    /**
     * Gets the additional properties to apply to the item.
     *
     * @return The additional properties
     */
    public List<ImmutableDataManipulator<?, ?>> getAdditionalProperties() {
        return this.additionalProperties;
    }

    /**
     * Gets a collection of between zero and {@code maxStacks} new
     * {@link ItemStack}s based on this {@link WeightedItem}.
     *
     * @param builder The builder to use to create the item stacks
     * @param rand The random object to use
     * @param maxStacks The maximum number of item stacks that may be created
     * @return The item stacks
     */
    public Collection<ItemStack> getRandomItem(ItemStackBuilder builder, Random rand, int maxStacks) {
        int total = this.quantity.getFlooredAmount(rand);
        if (total <= 0) {
            return new ArrayList<>();
        }
        ItemType type = get();
        int max = type.getMaxStackQuantity();
        if (total / max > maxStacks) {
            total = maxStacks * max;
        }
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < total;) {
            int n = (i + type.getMaxStackQuantity() > total) ? total - i : type.getMaxStackQuantity();
            builder.reset().itemType(type).quantity(n);
            for (ImmutableDataManipulator<?, ?> data : this.additionalProperties) {
                builder.itemData(data);
            }
            result.add(builder.build());
            i += n;
        }
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("object", this.object)
                .add("weight", this.weight)
                .add("quantity", this.quantity)
                .add("additionalProperties", this.additionalProperties)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        WeightedItem object = (WeightedItem) obj;
        if (!this.object.equals(object.object)) {
            return false;
        }
        if (!this.quantity.equals(object.quantity)) {
            return false;
        }
        if (!this.additionalProperties.equals(object.additionalProperties)) {
            return false;
        }
        return this.weight == object.weight;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(WEIGHTED_ITEM_TYPE, this.get().getId())
            .set(WEIGHTED_ITEM_DATA, this.additionalProperties)
            .set(WEIGHTED_ITEM_WEIGHT, this.weight)
            .set(WEIGHTED_ITEM_QUANTITY, this.quantity);
    }
}
