package com.forgeessentials.commands;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.permissions.IPermRegisterEvent;
import com.forgeessentials.api.permissions.RegGroup;
import com.forgeessentials.api.permissions.query.PermQueryPlayer;
import com.forgeessentials.commands.util.AFKdata;
import com.forgeessentials.commands.util.EventHandler;
import com.forgeessentials.commands.util.FEcmdModuleCommands;
import com.forgeessentials.util.ChatUtils;
import com.forgeessentials.util.OutputHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandAFK extends FEcmdModuleCommands {
    public static CommandAFK instance;
    public static List<UUID> afkList = new ArrayList<UUID>();
    // Config
    public static int warmup = 5;
    public static String outMessage, inMessage, selfOutMessage, selfInMessage;
    public final String NOTICEPERM = getCommandPerm() + ".notice";

    public CommandAFK()
    {
        instance = this;
    }

    @Override
    public void doConfig(Configuration config, String category)
    {
        warmup = config.get(category, "warmup", 5, "Time in sec. you have to stand still to activate AFK.").getInt();
        String messages = category + ".messages";
        outMessage = config.get(messages, "outMessage", "Player %s is now away").getString();
        inMessage = config.get(messages, "inMessage", "Player %s is no longer away").getString();
        selfOutMessage = config.get(messages, "selfOutMessage", "You are now away").getString();
        selfInMessage = config.get(messages, "selfInMessage", "You are no longer away").getString();
    }

    @Override
    public String getCommandName()
    {
        return "afk";
    }

    @Override
    public void processCommandPlayer(EntityPlayer sender, String[] args)
    {
        EventHandler.afkListToAdd.add(new AFKdata((EntityPlayerMP) sender));
        OutputHandler.chatConfirmation(sender, String.format("Stand still for %d seconds.", warmup));
    }

    @Override
    public boolean canConsoleUseCommand()
    {
        return false;
    }

    public void abort(AFKdata afkData)
    {
        if (!afkData.player.capabilities.isCreativeMode)
        {
            afkData.player.capabilities.disableDamage = false;
        }
        afkData.player.sendPlayerAbilities();
        afkList.remove(afkData.player.getPersistentID());
        EventHandler.afkListToRemove.add(afkData);

        if (APIRegistry.perms.checkPermAllowed(new PermQueryPlayer(afkData.player, NOTICEPERM)))
        {
            ChatUtils.sendMessage(MinecraftServer.getServer().getConfigurationManager(),
                    String.format(outMessage, afkData.player.getPersistentID()));
        }
        else
        {
            OutputHandler.chatConfirmation(afkData.player, selfOutMessage);
        }
    }

    public void makeAFK(AFKdata afkData)
    {
        afkData.player.capabilities.disableDamage = true;
        afkData.player.sendPlayerAbilities();
        afkList.add(afkData.player.getPersistentID());

        if (APIRegistry.perms.checkPermAllowed(new PermQueryPlayer(afkData.player, NOTICEPERM)))
        {
            ChatUtils.sendMessage(MinecraftServer.getServer().getConfigurationManager(),
                    String.format(inMessage, afkData.player.getPersistentID()));
        }
        else
        {
            OutputHandler.chatConfirmation(afkData.player, selfInMessage);
        }
    }

    @Override
    public void registerExtraPermissions(IPermRegisterEvent event)
    {
        event.registerPermissionLevel(NOTICEPERM, RegGroup.MEMBERS);
    }

    @Override
    public RegGroup getReggroup()
    {
        return RegGroup.MEMBERS;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {

        return "/afk Mark yourself as away.";
    }
}
