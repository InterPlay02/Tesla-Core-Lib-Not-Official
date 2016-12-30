//package net.ndrei.teslacorelib.inventory;
//
//import com.google.common.collect.Lists;
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.nbt.NBTTagList;
//import net.minecraftforge.common.util.Constants;
//import net.minecraftforge.common.util.INBTSerializable;
//import net.minecraftforge.items.ItemHandlerHelper;
//
//import javax.annotation.Nonnull;
//import java.util.Arrays;
//import java.util.List;
//
///**
// * Created by CF on 2016-12-15.
// */
//abstract class BasicItemHandler implements IFilteredItemHandler, INBTSerializable<NBTTagCompound> {
//    private int size = 0;
//    private ItemStack[] stacks = null;
//
//    protected BasicItemHandler(int slots) {
//        this.setSize(slots);
//    }
//
//    @Nonnull
//    private ItemStack[] setSize(int size) {
//        List<ItemStack> removed = Lists.newArrayList();
//        if (this.size != size) {
//            this.size = size;
//            if (this.stacks == null) {
//                this.stacks = new ItemStack[this.size];
//            }
//            else {
//                if (this.stacks.length > this.size) {
//                    for(int i = this.size; i < this.stacks.length; i++) {
//                        ItemStack stack = this.stacks[i];
//                        if ((stack != null) && !ItemStackUtil.isEmpty(stack)) {
//                            removed.add(stack);
//                        }
//                    }
//                }
//                this.stacks = Arrays.copyOf(this.stacks, this.size);
//            }
//            for(int i = this.size; i < this.stacks.length; i++) {
//                if (this.stacks[i] == null) {
//                    this.stacks[i] = ItemStackUtil.getEmptyStack();
//                }
//            }
//        }
//        return removed.toArray(new ItemStack[0]); // <-- java sucks
//    }
//
//    private void validateSlotIndex(int slot) {
//        if ((slot < 0) || (slot >= this.getSlots())) {
//            throw new IndexOutOfBoundsException("This inventory is not big enough!");
//        }
//    }
//
//    protected boolean isItemValidForSlot(int slot, ItemStack stack) {
//        return true;
//    }
//
//    @Override
//    public boolean canInsertItem(int slot, ItemStack stack) {
//        if (ItemStackUtil.isEmpty(stack)) {
//            // inserted stack is empty
//            return false;
//        }
//        ItemStack existing = this.getStackInSlot(slot);
//        if (!ItemStackUtil.isEmpty(existing) && !ItemHandlerHelper.canItemStacksStack(existing, stack)) {
//            // different items than existing stack
//            return false;
//        }
//        if (!ItemStackUtil.isEmpty(existing) && (this.getStackLimit(slot, existing) < (existing.getCount() + stack.getCount()))) {
//            // resulting stack would be too big
//            return false;
//        }
//        if (ItemStackUtil.isEmpty(existing) && !this.isItemValidForSlot(slot, stack)) {
//            // new stack not valid for this slot
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public boolean canExtractItem(int slot) {
//        ItemStack existing = this.getStackInSlot(slot);
//        return !ItemStackUtil.isEmpty(existing);
//    }
//
//    protected int getStackLimit(int slot, ItemStack stack) {
//        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
//    }
//
//    @Override
//    public int getSlotLimit(int slot) {
//        return 64;
//    }
//
//    protected void onContentsChanged(int slot) { }
//
//    protected void onLoaded() { }
//
//    @Override
//    public final int getSlots() {
//        return this.size;
//    }
//
//    @Nonnull
//    @Override
//    public final ItemStack getStackInSlot(int slot) {
//        this.validateSlotIndex(slot);
//        ItemStack stack = this.stacks[slot];
//        return (stack == null) ? ItemStackUtil.getEmptyStack() : stack;
//    }
//
//    @Nonnull
//    @Override
//    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
//        this.validateSlotIndex(slot);
//        if (ItemStackUtil.isEmpty(stack))
//            return ItemStackUtil.getEmptyStack();
//
//        if (!this.canInsertItem(slot, stack)) {
//            return stack;
//        }
//
//        ItemStack existing = this.getStackInSlot(slot);
//        int limit = this.getStackLimit(slot, stack);
//        if (!ItemStackUtil.isEmpty(existing)) {
//            if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
//                return stack;
//            }
//            limit -= existing.getCount();
//        }
//
//        if (limit <= 0) {
//            return stack;
//        }
//
//        boolean overflowed = stack.getCount() > limit;
//        if (!simulate) {
//            if (ItemStackUtil.isEmpty(existing)) {
//                this.stacks[slot] = overflowed
//                        ? ItemHandlerHelper.copyStackWithSize(stack, limit)
//                        : stack;
//            }
//            else {
//                ItemStackUtil.grow(existing, overflowed ? limit : stack.getCount());
//            }
//            this.onContentsChanged(slot);
//        }
//
//        return overflowed
//                ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit)
//                : ItemStackUtil.getEmptyStack();
//    }
//
//    @Nonnull
//    @Override
//    public ItemStack extractItem(int slot, int amount, boolean simulate) {
//        if (amount <= 0) {
//            return ItemStackUtil.getEmptyStack();
//        }
//        this.validateSlotIndex(slot);
//
//        ItemStack existing = this.getStackInSlot(slot);
//        if (ItemStackUtil.isEmpty(existing)) {
//            return ItemStackUtil.getEmptyStack();
//        }
//
//        int toExtract = Math.min(amount, existing.getMaxStackSize());
//        if (existing.getCount() <= toExtract) {
//            if (!simulate) {
//                this.stacks[slot] = ItemStackUtil.getEmptyStack();
//                this.onContentsChanged(slot);
//            }
//            return existing;
//        }
//        else {
//            if (!simulate) {
//                this.stacks[slot] = ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract);
//                this.onContentsChanged(slot);
//            }
//            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
//        }
//    }
//
//    @Override
//    public NBTTagCompound serializeNBT() {
//        NBTTagList list = new NBTTagList();
//
//        for (int i = 0; i < this.stacks.length; i++) {
//            if ((this.stacks[i] != null) && !ItemStackUtil.isEmpty(this.stacks[i])) {
//                NBTTagCompound itemTag = new NBTTagCompound();
//                itemTag.setInteger("SLOT", i);
//                this.stacks[i].writeToNBT(itemTag);
//                list.appendTag(itemTag);
//            }
//        }
//        NBTTagCompound nbt = new NBTTagCompound();
//        nbt.setTag("ITEMS", list);
//        nbt.setInteger("SIZE", this.stacks.length);
//        return nbt;
//    }
//
//    @Override
//    public void deserializeNBT(NBTTagCompound nbt) {
//        this.setSize(nbt.hasKey("SIZE", Constants.NBT.TAG_INT) ? nbt.getInteger("SIZE") : this.size);
//        NBTTagList tagList = nbt.getTagList("ITEMS", Constants.NBT.TAG_COMPOUND);
//        for (int i = 0; i < tagList.tagCount(); i++) {
//            NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
//            int slot = itemTags.getInteger("SLOT");
//            if ((slot >= 0) && (slot < this.stacks.length)) {
//                this.stacks[slot] = new ItemStack(itemTags);
//            }
//        }
//        this.onLoaded();
//    }
//}
