package com.blakebr0.mysticalagriculture.api.lib;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class LazyIngredient {
    public static final LazyIngredient EMPTY = new LazyIngredient(null, null, null) {
        @Override
        public Ingredient getIngredient() {
            return Ingredient.EMPTY;
        }
    };

    private final String name;
    private final CompoundNBT nbt;
    private final Type type;
    private Ingredient ingredient;

    private LazyIngredient(String name, Type type, CompoundNBT nbt) {
        this.name = name;
        this.type = type;
        this.nbt = nbt;
    }

    public static LazyIngredient item(String name) {
        return item(name, null);
    }

    public static LazyIngredient item(String name, CompoundNBT nbt) {
        return new LazyIngredient(name, Type.ITEM, nbt);
    }

    public static LazyIngredient tag(String name) {
        return new LazyIngredient(name, Type.TAG, null);
    }

    public boolean isItem() {
        return this.type == Type.ITEM;
    }

    public boolean isTag() {
        return this.type == Type.TAG;
    }

    public Ingredient.IItemList createItemList() {
        if (this.isTag()) {
            ITag<Item> tag = TagCollectionManager.getInstance().getItems().getTag(new ResourceLocation(this.name));
            if (tag != null) {
                return new Ingredient.TagList(tag);
            }
        } else if (this.isItem()) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(this.name));
            if (item != null) {
                if (this.nbt != null && !this.nbt.isEmpty()) {
                    ItemStack stack = new ItemStack(item);
                    stack.setTag(this.nbt);

                    return new Ingredient.SingleItemList(stack);
                }

                return new Ingredient.SingleItemList(new ItemStack(item));
            }
        }

        return null;
    }

    public Ingredient getIngredient() {
        if (this.ingredient == null) {
            if (this.isTag()) {
                ITag<Item> tag = TagCollectionManager.getInstance().getItems().getTag(new ResourceLocation(this.name));
                if (tag != null && !tag.getValues().isEmpty())
                    this.ingredient = Ingredient.of(tag);
            } else if (this.isItem()) {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(this.name));
                if (item != null) {
                    if (this.nbt == null || this.nbt.isEmpty()) {
                        this.ingredient = Ingredient.of(item);
                    } else {
                        ItemStack stack = new ItemStack(item);
                        stack.setTag(this.nbt);
                        this.ingredient = new NBTIngredient(stack);
                    }
                }
            }
        }

        return this.ingredient == null ? Ingredient.EMPTY : this.ingredient;
    }

    private enum Type {
        ITEM, TAG
    }

    private static class NBTIngredient extends net.minecraftforge.common.crafting.NBTIngredient {
        private NBTIngredient(ItemStack stack) {
            super(stack);
        }
    }
}
