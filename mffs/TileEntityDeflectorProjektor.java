package mffs;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import net.minecraft.src.Container;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_ModularForceFieldSystem;

public class TileEntityDeflectorProjektor extends TileEntityProjektor {

	private int lengthx;
	private int lengthz;
	private int maxlengthx;
	private int maxlengthz;
	private int distance;
	private int maxdistance;

	public TileEntityDeflectorProjektor() {

		lengthx = 0;
		lengthz = 0;
		distance = 0;
		maxlengthx = 32;
		maxlengthz = 32;
		maxdistance = 10;

	}

	public Container getContainer(InventoryPlayer inventoryplayer) {
		return new ContainerProjektor(inventoryplayer, this);
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {

		super.readFromNBT(nbttagcompound);
		lengthx = nbttagcompound.getInteger("lengthx");
		lengthz = nbttagcompound.getInteger("lengthz");
		distance = nbttagcompound.getInteger("distance");

	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {

		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("lengthx", lengthx);
		nbttagcompound.setInteger("lengthz", lengthz);
		nbttagcompound.setInteger("distance", distance);

	}

	public int getMaxlengthx() {
		return maxlengthx;
	}

	public int getlengthx() {
		return lengthx;
	}

	public int getMaxlengthz() {
		return maxlengthz;
	}

	public int getlengthz() {
		return lengthz;
	}

	public int getDistance() {
		return distance;
	}

	public int getMaxdistance() {
		return maxdistance;
	}

	public void setDistance(int distance) {

		this.distance = distance;

	}

	public void setDistanceinit(int distance) {

		this.distance = distance;

	}

	public void setlengthx(int lengthx) {

		this.lengthx = lengthx;

	}

	public void setlengthxinit(int lengthx) {

		this.lengthx = lengthx;

	}

	public void setlengthz(int lengthz) {

		this.lengthz = lengthz;
	}

	public void setlengthzinit(int lengthz) {

		this.lengthz = lengthz;

	}

	public void addtogrid() {

		if (Functions.isSimulation()) {
			Linkgrid.getWorldMap(worldObj).getProjektor().put(getProjektor_ID(), this);
		}
	}

	public void removefromgrid() {

		if (Functions.isSimulation()) {
			Linkgrid.getWorldMap(worldObj).getProjektor().remove(getProjektor_ID());

		}
	}

	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side) {

		if (this.getFacing() != side && (!this.getActive())) {
			this.setFacing((short) side);

			worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
			return true;
		}
		return false;
	}

	public void updateEntity() {

		if (Functions.isSimulation()) {

			if (this.isCreate() && this.getLinkGenerator_ID() != 0) {
				addtogrid();
				addfreqcard();
				this.setCreate(false);
				if (this.getActive()) {
					checkupdates();
					fieldcalculation(true);
				}
			}

			if (this.getLinkGenerator_ID() != 0) {
				this.setLinkGenerator(true);
				try {
					this.setLinkPower(Linkgrid.getWorldMap(worldObj).getGenerator().get(this.getLinkGenerator_ID()).getForcepower());
					this.setMaxlinkPower(Linkgrid.getWorldMap(worldObj).getGenerator().get(this.getLinkGenerator_ID()).getMaxforcepower());
				} catch (java.lang.NullPointerException ex) {
					this.setLinkGenerator(false);
					this.setLinkPower(0);
					this.setMaxlinkPower(1000000);
				}
			} else {
				this.setLinkGenerator(false);
				this.setLinkPower(0);
				this.setMaxlinkPower(1000000);
			}

			boolean powerdirekt = worldObj.isBlockGettingPowered(xCoord, yCoord, zCoord);
			boolean powerindrekt = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);

			if ((powerdirekt || powerindrekt) && this.isLinkGenerator() && this.getLinkPower() > Forcepowerneed(lengthx * lengthz, true)) {

				if (getActive() != true) {
					setActive(true);
					fieldcalculation(false);
					FieldGenerate(true);
					worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
				}
			}
			if ((!powerdirekt && !powerindrekt) || !this.isLinkGenerator() || this.getLinkPower() < Forcepowerneed(lengthx * lengthz, false)) {

				if (getActive() != false) {
					setActive(false);
					destroyField();
					worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
				}
			}

			if (getActive() && getWrenchDropRate() != -1.0F) {
				setWrenchRate(-1.0F);
			}
			if (!getActive() && getWrenchDropRate() != 1.0F) {
				setWrenchRate(1.0F);
			}

			if (getActive()) {
				FieldGenerate(false);
			}

			if (this.getTicker() == 10) {
				checkupdates();
				addfreqcard();
				this.setTicker((short) 0);
			}
			this.setTicker((short) (this.getTicker() + 1));
		}
	}

	public void checkupdates() {

		this.setHardner(false);
		this.setZapper(false);
		this.setCamouflage(false);

		for (int x = xCoord - 1; x <= xCoord + 1; x++) {
			for (int y = yCoord - 1; y <= yCoord + 1; y++) {
				for (int z = zCoord - 1; z <= zCoord + 1; z++) {

					if (worldObj.getBlockId(x, y, z) == mod_ModularForceFieldSystem.MFFSUpgrades.blockID) {

						int meta = worldObj.getBlockMetadata(x, y, z);

						TileEntity upgrades = worldObj.getBlockTileEntity(x, y, z);
						if (upgrades != null) {
							if (((TileEntityPassivUpgrade) upgrades).getconectet_ID() == 0 && (meta == 3 || meta == 6 || meta == 7)) {
								((TileEntityPassivUpgrade) upgrades).setconectet_ID(getProjektor_ID());
								((TileEntityPassivUpgrade) upgrades).setConnectet_typID((short) 2);
								worldObj.markBlockAsNeedsUpdate(upgrades.xCoord, upgrades.yCoord, upgrades.zCoord);
							}

							if (meta == 7) {
								if (((TileEntityCamoflageUpgrade) upgrades).getItem_ID() != getTextur()) {
									this.setTextur(((TileEntityCamoflageUpgrade) upgrades).getItem_ID());
								}

							}

							if (((TileEntityPassivUpgrade) upgrades).getconectet_ID() == getProjektor_ID() && ((TileEntityPassivUpgrade) upgrades).getConnectet_typID() == 2) {

								if (((TileEntityPassivUpgrade) upgrades).getActive() != this.getActive()) {
									((TileEntityPassivUpgrade) upgrades).setActive(this.getActive());
								}

								switch (meta) {
								case 3:
									this.setHardner(true);
									break;
								case 6:
									this.setZapper(true);
									break;
								case 7:
									this.setCamouflage(true);
									break;
								}
							}
						}
					}
				}
			}
		}

		if (this.isZapper()) {
			this.setffmeta((short) 1);
		} else {
			this.setffmeta((short) 0);
		}
		if (this.isCamouflage()) {
			this.setffmeta((short) 2);
		}

	}

	public void fieldcalculation(boolean create) {

		if (!field_queue.isEmpty()) {
			field_queue.clear();
		}

		int tempx = 0;
		int tempy = 0;
		int tempz = 0;

		for (int x1 = 0 - getlengthx(); x1 < getlengthx() + 1; x1++) {
			for (int z1 = 0 - getlengthz(); z1 < getlengthz() + 1; z1++) {

				if (this.getFacing() == 0) {
					tempy = 0 - distance - 1;
					tempx = x1;
					tempz = z1;
				}

				if (this.getFacing() == 1) {
					tempy = 0 + distance + 1;
					tempx = x1;
					tempz = z1;
				}

				if (this.getFacing() == 2) {
					tempz = 0 - distance - 1;
					tempy = x1;
					tempx = z1;
				}

				if (this.getFacing() == 3) {
					tempz = 0 + distance + 1;
					tempy = x1;
					tempx = z1;
				}

				if (this.getFacing() == 4) {
					tempx = 0 - distance - 1;
					tempy = x1;
					tempz = z1;
				}
				if (this.getFacing() == 5) {
					tempx = 0 + distance + 1;
					tempy = x1;
					tempz = z1;
				}

				hasher.setLength(0);
				hasher.append(xCoord + tempx).append("/").append(yCoord + tempy).append("/").append(zCoord + tempz);

				ForceFieldWorldMap ffworldmap = WorldMap.getForceFieldforWorld(worldObj).addandgetffmp(xCoord + tempx, yCoord + tempy, zCoord + tempz);

				boolean freespace = !worldObj.getBlockMaterial(xCoord + tempx, yCoord + tempy, zCoord + tempz).isSolid();
				boolean ffblockinplace = worldObj.getBlockId(xCoord + tempx, yCoord + tempy, zCoord + tempz) == mod_ModularForceFieldSystem.MFFSFieldblock.blockID;

				if (ffblockinplace) {
					freespace = true;
				}

				if (ffworldmap.listsize() == 0) {
					if (this.getffmeta() == 2) {
						TexturworldMap.getTexturMap(worldObj.worldProvider.worldType).add(xCoord + tempx, yCoord + tempy, zCoord + tempz, this.getTextur());
					}
					ffworldmap.ffworld_addFirst(new ForceFieldBlock(this.getLinkGenerator_ID(), this.getProjektor_ID(), freespace, true, this.getffmeta()));
					field_queue.add(hasher.toString());

				} else {
					ffworldmap.ffworld_addLast(new ForceFieldBlock(this.getLinkGenerator_ID(), this.getProjektor_ID(), freespace, false, this.getffmeta()));
					field_queue.add(hasher.toString());
				}
			}
		}

	}

	public void destroyField() {

		for (String hasher : field_queue) {
			ForceFieldWorldMap ffworldmap = WorldMap.getForceFieldforWorld(worldObj).getFFWM(hasher.toString());
			if (ffworldmap.listsize() >= 1) {

				if (ffworldmap.ffworld_getfirstfreespace() == true && ffworldmap.ffworld_getfistactive() && ffworldmap.ffworld_getfirstProjektor_ID() == getProjektor_ID()) {
					ffworldmap.ffworld_remove(getProjektor_ID());
					worldObj.setBlockWithNotify(ffworldmap.getX(), ffworldmap.getY(), ffworldmap.getZ(), 0);
					if (ffworldmap.listsize() >= 1) {

						ffworldmap.ffworld_setfistactive(true);
						ffworldmap.ffworld_setfirstfreeospace(false);

					}
				} else {
					ffworldmap.ffworld_remove(getProjektor_ID());
				}

			}
		}
		field_queue.clear();
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		// TODO Auto-generated method stub
		return null;
	}

}
