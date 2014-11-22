package com.forgeessentials.multiworld.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.StartupQuery;

public class MultiworldSaveHandler implements ISaveHandler {

    private SaveHandler parent;

    private Multiworld world;

    public MultiworldSaveHandler(ISaveHandler parent, Multiworld world)
    {
        if (!(parent instanceof SaveHandler))
            throw new RuntimeException();
        this.parent = (SaveHandler) parent;
        this.world = world;
    }

    public File getDimensionDirectory()
    {
        return new File(getWorldDirectory(), "FEMultiworld/" + world.getName());
    }

    @Override
    public IChunkLoader getChunkLoader(WorldProvider provider)
    {
        return new AnvilChunkLoader(getDimensionDirectory());
    }

    @Override
    public WorldInfo loadWorldInfo()
    {
        File file1 = new File(getDimensionDirectory(), "level.dat");
        if (file1.exists())
        {
            try
            {
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file1));
                NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Data");
                WorldInfo worldInfo = new WorldInfo(nbttagcompound1);
                // TODO: Check if this call should really stay here or needs to be removed!!
                FMLCommonHandler.instance().handleWorldDataLoad(parent, worldInfo, nbttagcompound);
                return worldInfo;
            }
            catch (StartupQuery.AbortedException e)
            {
                throw e;
            }
            catch (Exception exception1)
            {
                exception1.printStackTrace();
            }
        }
        
        file1 = new File(getDimensionDirectory(), "level.dat_old");
        if (file1.exists())
        {
            try
            {
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file1));
                NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Data");
                WorldInfo worldInfo = new WorldInfo(nbttagcompound1);
                // TODO: Check if this call should really stay here or needs to be removed!!
                FMLCommonHandler.instance().handleWorldDataLoad(parent, worldInfo, nbttagcompound);
                return worldInfo;
            }
            catch (StartupQuery.AbortedException e)
            {
                throw e;
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public void checkSessionLock() throws MinecraftException
    {
        parent.checkSessionLock();
    }

    public void saveWorldInfoData(WorldInfo p_75755_1_, NBTTagCompound data)
    {
        NBTTagCompound dataTag = new NBTTagCompound();
        dataTag.setTag("Data", data);
        
        // TODO: Check if this call should really stay here or needs to be removed!!
        FMLCommonHandler.instance().handleWorldDataSave(parent, p_75755_1_, dataTag);

        try
        {
            File file1 = new File(getDimensionDirectory(), "level.dat_new");
            File file2 = new File(getDimensionDirectory(), "level.dat_old");
            File file3 = new File(getDimensionDirectory(), "level.dat");
            CompressedStreamTools.writeCompressed(dataTag, new FileOutputStream(file1));

            if (file2.exists())
            {
                file2.delete();
            }
            file3.renameTo(file2);
            if (file3.exists())
            {
                file3.delete();
            }
            file1.renameTo(file3);
            if (file1.exists())
            {
                file1.delete();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public void saveWorldInfoWithPlayer(WorldInfo worldInfo, NBTTagCompound playerInfo)
    {
        saveWorldInfoData(worldInfo, worldInfo.cloneNBTCompound(playerInfo));
    }

    @Override
    public void saveWorldInfo(WorldInfo worldInfo)
    {
        saveWorldInfoData(worldInfo, worldInfo.getNBTTagCompound());
    }

    @Override
    public IPlayerFileData getSaveHandler()
    {
        return parent.getSaveHandler();
    }

    @Override
    public void flush()
    {
        parent.flush();
    }

    @Override
    public File getWorldDirectory()
    {
        return parent.getWorldDirectory();
    }

    @Override
    public File getMapFileFromName(String name)
    {
        return parent.getMapFileFromName(name);
    }

    @Override
    public String getWorldDirectoryName()
    {
        return parent.getWorldDirectoryName();
    }

}