package vazkii.botania.common.block.tile;

import java.util.Random;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import vazkii.botania.api.mana.IManaReceiver;

import com.bioxx.tfc.Items.ItemBloom;
import com.bioxx.tfc.Items.ItemMeltedMetal;
import com.bioxx.tfc.TileEntities.NetworkTileEntity;
import com.bioxx.tfc.api.*;
import com.bioxx.tfc.api.Interfaces.ISmeltable;


public class TEForge extends NetworkTileEntity implements IInventory ,IManaReceiver
{
	public float fireTemp;
	public int maxFireTempScale = 2000; // Fixes NPE
	public ItemStack fireItemStacks[];
    int mana;
	private int time;
    private static final int MAX_MANA=1280;
	public TEForge()
	{
		super();

		fireItemStacks = new ItemStack[9];
		maxFireTempScale = 2500;
	}

	public void careForInventorySlot(ItemStack is)
	{
		if(is != null)
		{
			HeatRegistry manager = HeatRegistry.getInstance();
			HeatIndex index = manager.findMatchingIndex(is);

			if (index != null && index.hasOutput())
			{
				float temp = TFC_ItemHeat.getTemp(is);
				if (fireTemp > temp && fireTemp <1000)
				{
					temp += TFC_ItemHeat.getTempIncrease(is);
					TFC_ItemHeat.setTemp(is, temp);
				}
				else if (fireTemp > temp && fireTemp <2200)
				{
					temp += TFC_ItemHeat.getTempIncrease(is)*2;
					TFC_ItemHeat.setTemp(is, temp);
				}
				else if (fireTemp > temp && fireTemp >=2200)
				{
					temp += TFC_ItemHeat.getTempIncrease(is)*4;
					TFC_ItemHeat.setTemp(is, temp);
				}
				else
				{
					temp -= TFC_ItemHeat.getTempDecrease(is);
				TFC_ItemHeat.setTemp(is, temp);
				}
			}
		}
	}



	public int getMaxTemp()
	{
		return 2500;
	}

	public int getTemperatureScaled(int height)
	{
		return (int)(Math.min(1800,fireTemp)* height / 1800);
	}





	
	/*
	 * public void handleTempFlux(float desiredTemp) { if(fireTemp < desiredTemp) {
	 * if(airFromBellows == 0) fireTemp++; else fireTemp += 2; } else if(fireTemp >
	 * desiredTemp) { if(desiredTemp == 0) { if(airFromBellows == 0) fireTemp -= 1;
	 * else fireTemp -= 0.5; } } this.keepTempToRange(); }
	 */
	 







	@Override
	public void handleDataPacket(NBTTagCompound nbt)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void createDataNBT(NBTTagCompound nbt)
	{
		// TODO Auto-generated method stub
	}



	/*private class CoordDirection
	{
		int x; int y; int z; ForgeDirection dir;
		public CoordDirection(int x, int y, int z, ForgeDirection d)
		{
			this.x = x;this.y = y;this.z = z;this.dir = d;
		}
	}*/

	@Override
	public void closeInventory()
	{
	}

	public void combineMetals(ItemStack inputItem, ItemStack destItem)
	{
		int d1 = 100 - inputItem.getItemDamage();
		int d2 = 100 - destItem.getItemDamage();
		destItem.setItemDamage(100 - (d1 + d2));
	}

	public void cookItem(int i)
	{
		HeatRegistry manager = HeatRegistry.getInstance();
		Random r = new Random();
		if(fireItemStacks[i] != null)
		{
			HeatIndex index = manager.findMatchingIndex(fireItemStacks[i]);
			ItemStack inputCopy = fireItemStacks[i].copy();
			
			if(index != null && TFC_ItemHeat.getTemp(fireItemStacks[i]) > index.meltTemp)
			{
				float temperature = TFC_ItemHeat.getTemp(fireItemStacks[i]);
				//int dam = fireItemStacks[i].getItemDamage();

				// If not unshaped metal, morph the input to the output. If not an input with direct morph (sand, sticks, etc) this deletes the input item from the slot.
				if(!(fireItemStacks[i].getItem() instanceof ItemMeltedMetal))
					fireItemStacks[i] = index.getMorph();

				// Handle items that had a direct morph.
				if(fireItemStacks[i] != null)
				{
					HeatIndex morphIndex = manager.findMatchingIndex(fireItemStacks[i]);
					if(morphIndex != null)
					{
						// Apply old temperature to direct morphs that can continue to be heated.
						TFC_ItemHeat.setTemp(fireItemStacks[i], temperature);
					}
				}
				else if(index.hasOutput())
				{
					ItemStack output = index.getOutput(inputCopy, r);
					if (inputCopy.getItem() instanceof ISmeltable)
					{
						ISmeltable smelt = (ISmeltable)inputCopy.getItem();
						ItemStack meltedItem = new ItemStack(smelt.getMetalType(inputCopy).meltedItem);
						TFC_ItemHeat.setTemp(meltedItem, temperature);

						int units = smelt.getMetalReturnAmount(inputCopy);
						// Raw/Refined Blooms give at max 100 units to force players to split using the anvil
						if (inputCopy.getItem() instanceof ItemBloom)
							units = Math.min(100, units);

						while(units > 0 && getMold() != null)
						{
							ItemStack moldIS = this.getMold();
							ItemStack outputCopy = meltedItem.copy();

							if (units > 100)
							{
								units-= 100;
								moldIS.stackSize--;
								if(!addToStorage(outputCopy.copy()))
								{
									EntityItem ei = new EntityItem(worldObj, xCoord + 0.5, yCoord + 1.5, zCoord + 0.5, outputCopy);
									ei.motionX = 0; ei.motionY = 0; ei.motionZ = 0;
									worldObj.spawnEntityInWorld(ei);
								}
							}
							else if (units > 0) // Put the last item in the forge cooking slot, replacing the input.
							{
								outputCopy.setItemDamage(100-units);
								units = 0;
								moldIS.stackSize--;
								fireItemStacks[i] = outputCopy.copy();
							}
						}
					}
					else
					{
						if(output.getItem() instanceof ItemMeltedMetal){
							if(getMold()==null||getMold().stackSize==0)return;
							if(getMold().stackSize< output.stackSize){
								fireItemStacks[i]= new ItemStack(output.getItem(),getMold().stackSize, output.getItemDamage());
								getMold().stackSize=0;
							}else {
								fireItemStacks[i] = output;
								getMold().stackSize-= output.stackSize;
							}
						}
						else fireItemStacks[i] = output;
					}


					if(TFC_ItemHeat.isCookable(fireItemStacks[i]) > -1)
					{
						//if the input is a new item, then apply the old temperature to it
						TFC_ItemHeat.setTemp(fireItemStacks[i], temperature);
					}
				}
			}
		}
	}

	public boolean addToStorage(ItemStack is)
	{
		if(this.getStackInSlot(5) == null)
		{
			this.setInventorySlotContents(5, is);
			return true;
		}
		if(this.getStackInSlot(6) == null)
		{
			this.setInventorySlotContents(6, is);
			return true;
		}
		if(this.getStackInSlot(7) == null)
		{
			this.setInventorySlotContents(7, is);
			return true;
		}
		if(this.getStackInSlot(8) == null)
		{
			this.setInventorySlotContents(8, is);
			return true;
		}
		return false;
	}

	private ItemStack getMold()
	{
		if(fireItemStacks[5] != null && fireItemStacks[5].getItem() == TFCItems.ceramicMold && fireItemStacks[5].stackSize > 0)
		{
			return fireItemStacks[5];
		}
		else if(fireItemStacks[6] != null && fireItemStacks[6].getItem() == TFCItems.ceramicMold && fireItemStacks[6].stackSize > 0)
		{
			return fireItemStacks[6];
		}
		else if(fireItemStacks[7] != null && fireItemStacks[7].getItem() == TFCItems.ceramicMold && fireItemStacks[7].stackSize > 0)
		{
			return fireItemStacks[7];
		}
		else if(fireItemStacks[8] != null && fireItemStacks[8].getItem() == TFCItems.ceramicMold && fireItemStacks[8].stackSize > 0)
		{
			return fireItemStacks[8];
		}
		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		if(fireItemStacks[i] != null)
		{
			if(fireItemStacks[i].stackSize <= j)
			{
				ItemStack is = fireItemStacks[i];
				fireItemStacks[i] = null;
				return is;
			}

			ItemStack isSplit = fireItemStacks[i].splitStack(j);
			if(fireItemStacks[i].stackSize == 0)
				fireItemStacks[i] = null;
			return isSplit;
		}
		else
			return null;
	}

	public void ejectContents()
	{
		float f3 = 0.05F;
		EntityItem entityitem;
		Random rand = new Random();
		float f = rand.nextFloat() * 0.8F + 0.1F;
		float f1 = rand.nextFloat() * 0.8F + 0.4F;
		float f2 = rand.nextFloat() * 0.8F + 0.1F;

		for (int i = 0; i < getSizeInventory(); i++)
		{
			if(fireItemStacks[i]!= null)
			{
				entityitem = new EntityItem(worldObj, xCoord + f, yCoord + f1, zCoord + f2, fireItemStacks[i]);
				entityitem.motionX = (float)rand.nextGaussian() * f3;
				entityitem.motionY = (float)rand.nextGaussian() * f3 + 0.2F;
				entityitem.motionZ = (float)rand.nextGaussian() * f3;
				worldObj.spawnEntityInWorld(entityitem);
				fireItemStacks[i] = null;
			}
		}
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public String getInventoryName()
	{
		return "Forge";
	}

	public int getMoldIndex()
	{
		if(fireItemStacks[5] != null && fireItemStacks[5].getItem() == TFCItems.ceramicMold)
			return 5;
		if(fireItemStacks[6] != null && fireItemStacks[6].getItem() == TFCItems.ceramicMold)
			return 6;
		if(fireItemStacks[7] != null && fireItemStacks[7].getItem() == TFCItems.ceramicMold)
			return 7;
		if(fireItemStacks[8] != null && fireItemStacks[8].getItem() == TFCItems.ceramicMold)
			return 8;
		return -1;
	}

	@Override
	public int getSizeInventory()
	{
		return fireItemStacks.length;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return fireItemStacks[i];
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1)
	{
		return null;
	}

	

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return false;
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		NBTTagList nbttaglist = nbt.getTagList("Items", 10);
		fireItemStacks = new ItemStack[getSizeInventory()];
		for(int i = 0; i < nbttaglist.tagCount(); i++)
		{
			NBTTagCompound nbt1 = nbttaglist.getCompoundTagAt(i);
			byte byte0 = nbt1.getByte("Slot");
			if(byte0 >= 0 && byte0 < fireItemStacks.length)
				fireItemStacks[byte0] = ItemStack.loadItemStackFromNBT(nbt1);
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		fireItemStacks[i] = itemstack;
		if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
			itemstack.stackSize = getInventoryStackLimit();
	}

	@Override
	public void updateEntity()
	{
		//Here we make sure that the forge is valid


		if(!worldObj.isRemote)
		{
			//Here we take care of the items that we are cooking in the fire
			careForInventorySlot(fireItemStacks[0]);
			careForInventorySlot(fireItemStacks[1]);
			careForInventorySlot(fireItemStacks[2]);
			careForInventorySlot(fireItemStacks[3]);
			careForInventorySlot(fireItemStacks[4]);

			ItemStack[] fuelStack = new ItemStack[4];
			fuelStack[0] = fireItemStacks[5];
			fuelStack[1] = fireItemStacks[6];
			fuelStack[2] = fireItemStacks[7];
			fuelStack[3] = fireItemStacks[8];

			//Now we cook the input item
			cookItem(0);
			cookItem(1);
			cookItem(2);
			cookItem(3);
			cookItem(4);
			if( mana  >=150 &&  fireTemp<1000)
			{

				fireTemp +=50;
                 mana-=150;
                 time =80;
			}
			else if( mana  >=300 &&  fireTemp<2000)
			{

				fireTemp +=50;
                 mana-=300;
                 time =80;
			}
			else if( mana  >=400 &&  fireTemp < 2500)
			{

				fireTemp +=25;
                 mana-=400;
                 time =80;
			}
			if (time > 0) {
			    time--;
			}
				
			if (fireTemp > 0 && time <= 0) {
				fireTemp-=10;
				time =80;
			}
			//Play the fire sound
			Random r = new Random();
			if(r.nextInt(10) == 0 && fireTemp > 20)
				worldObj.playSoundEffect(xCoord, yCoord, zCoord, "fire.fire", 0.4F + (r.nextFloat() / 2), 0.7F + r.nextFloat());



			

			//Here we handle the bellows


			//do a last minute check to verify stack size
			for(int c = 0; c < 5; c++)
			{
				if(fireItemStacks[c] != null)
				{
					if(fireItemStacks[c].stackSize <= 0)
						fireItemStacks[c].stackSize = 1;
				}
			}
			if(fireItemStacks[5]!=null&&fireItemStacks[5].stackSize<= 0)fireItemStacks[5]=null;
			if(fireItemStacks[6]!=null&&fireItemStacks[6].stackSize<= 0)fireItemStacks[6]=null;
			if(fireItemStacks[7]!=null&&fireItemStacks[7].stackSize<= 0)fireItemStacks[7]=null;
			if(fireItemStacks[8]!=null&&fireItemStacks[8].stackSize<= 0)fireItemStacks[8]=null;
		}
	}







	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return false;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		NBTTagList nbttaglist = new NBTTagList();
		for(int i = 0; i < fireItemStacks.length; i++)
		{
			if(fireItemStacks[i] != null)
			{
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("Slot", (byte)i);
				fireItemStacks[i].writeToNBT(nbt1);
				nbttaglist.appendTag(nbt1);
			}
		}
		nbt.setTag("Items", nbttaglist);
	}


	@Override
	public int getCurrentMana() {
		return mana ;
	}


	@Override
	public boolean isFull() {
		return mana >= MAX_MANA;
	}

	@Override
	public void recieveMana(int mana) {
		this.mana = Math.min(MAX_MANA, this.mana + mana);
	}

	@Override
	public boolean canRecieveManaFromBursts() {

		return true;
	}



	@Override
	public void createInitNBT(NBTTagCompound arg0) {

		
	}





	@Override
	public void handleInitPacket(NBTTagCompound arg0) {

		
	}


}
