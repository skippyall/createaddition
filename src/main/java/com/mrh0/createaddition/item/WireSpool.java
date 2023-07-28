package com.mrh0.createaddition.item;

import java.util.List;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.energy.IWireNode;
import com.mrh0.createaddition.energy.WireConnectResult;
import com.mrh0.createaddition.energy.WireType;
import com.mrh0.createaddition.index.CAItems;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WireSpool extends Item {

	public WireSpool(Properties props) {
		super(props);
	}
	
	@Override
	public InteractionResult useOn(UseOnContext c) {
		CompoundTag nbt = c.getItemInHand().getTag();
		if(nbt == null)
			nbt = new CompoundTag();
		
		BlockEntity te = c.getLevel().getBlockEntity(c.getClickedPos());
		if(te == null)
			return InteractionResult.PASS;
		if(!(te instanceof IWireNode))
			return InteractionResult.PASS;
		IWireNode node = (IWireNode) te;
		
		if(hasPos(nbt)) {
			WireConnectResult result;
			
			WireType connectionType = IWireNode.getTypeOfConnection(c.getLevel(), c.getClickedPos(), getPos(nbt));
			
			if(isRemover(c.getItemInHand().getItem()))
				result = IWireNode.disconnect(c.getLevel(), c.getClickedPos(), getPos(nbt));
			else
				result = IWireNode.connect(c.getLevel(), getPos(nbt), getNode(nbt), c.getClickedPos(), node.getAvailableNode(c.getClickLocation()), getWireType(c.getItemInHand().getItem()));

			// Play sound
			if(result.isLinked()) {
				c.getLevel().playLocalSound(c.getClickedPos().getX(), c.getClickedPos().getY(), c.getClickedPos().getZ(), SoundEvents.NOTE_BLOCK_XYLOPHONE, SoundSource.BLOCKS, .7f, 1f, false);
			}
			else if(result.isConnect()) {
				System.out.println("Connect");
				c.getLevel().playLocalSound(c.getClickedPos().getX(), c.getClickedPos().getY(), c.getClickedPos().getZ(), SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1f, 1f, false);
			}
			else if(result == WireConnectResult.REMOVED) {
				c.getLevel().playLocalSound(c.getClickedPos().getX(), c.getClickedPos().getY(), c.getClickedPos().getZ(), SoundEvents.NOTE_BLOCK_XYLOPHONE, SoundSource.BLOCKS, .7f, .5f, false);
			}
			else {
				c.getLevel().playLocalSound(c.getClickedPos().getX(), c.getClickedPos().getY(), c.getClickedPos().getZ(), SoundEvents.NOTE_BLOCK_DIDGERIDOO, SoundSource.BLOCKS, .7f, 1f, false);
			}

			te.setChanged();
			
			if(!c.getPlayer().isCreative()) {
				if(result == WireConnectResult.REMOVED) {
					c.getItemInHand().shrink(1);
					ItemStack stack = connectionType.getSourceDrop();
					boolean shouldDrop = !c.getPlayer().addItem(stack);
					if(shouldDrop)
						c.getPlayer().drop(stack, false);
				}
				else if(result.isLinked()) {
					c.getItemInHand().shrink(1);
					ItemStack stack = new ItemStack(CAItems.SPOOL.get(), 1);
					boolean shouldDrop = !c.getPlayer().addItem(stack);
					if(shouldDrop)
						c.getPlayer().drop(stack, false);
				}
			}
			c.getItemInHand().setTag(null);
			c.getPlayer().displayClientMessage(result.getMessage(), true);
			
		}
		else {
			int index = node.getAvailableNode(c.getClickLocation());
			if(index < 0)
				return InteractionResult.PASS;
			if(!isRemover(c.getItemInHand().getItem()))
				c.getPlayer().displayClientMessage(WireConnectResult.getConnect(node.isNodeInput(index), node.isNodeOutput(index)).getMessage(), true);
			c.getItemInHand().setTag(null);
			c.getItemInHand().setTag(setContent(nbt, node.getPos(), index));
		}
		return InteractionResult.CONSUME;
	}
	
	public static boolean hasPos(CompoundTag nbt) {
		if(nbt == null)
			return false;
    	return nbt.contains("x") && nbt.contains("y") && nbt.contains("z") && nbt.contains("node");
    }
	
	public static BlockPos getPos(CompoundTag nbt){
		if(nbt == null)
			return null;
    	return new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
    }
	
	public static int getNode(CompoundTag nbt){
		if(nbt == null)
			return -1;
    	return nbt.getInt("node");
    }
	
	public static CompoundTag setContent(CompoundTag nbt, BlockPos pos, int node){
		if(nbt == null)
			return new CompoundTag();
    	nbt.putInt("x", pos.getX());
    	nbt.putInt("y", pos.getY());
    	nbt.putInt("z", pos.getZ());
    	nbt.putInt("node", node);
    	return nbt;
    }
	
	@Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		CompoundTag nbt = stack.getTag();
    	super.appendHoverText(stack, worldIn, tooltip, flagIn);
    	if(hasPos(nbt))
    		tooltip.add(new TranslatableComponent("item."+CreateAddition.MODID+".spool.nbt"));
    }
	
	public static WireType getWireType(Item item) {
		if(item == CAItems.COPPER_SPOOL.get())
			return WireType.COPPER;
		if(item == CAItems.GOLD_SPOOL.get())
			return WireType.GOLD;
		if(item == CAItems.FESTIVE_SPOOL.get())
			return WireType.FESTIVE;
		return WireType.COPPER;
	}
	
	public static boolean isRemover(Item item) {
		return item == CAItems.SPOOL.get();
	}
}
