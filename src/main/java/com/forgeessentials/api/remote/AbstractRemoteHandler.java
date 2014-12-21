package com.forgeessentials.api.remote;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.forgeessentials.api.APIRegistry;

/**
 *
 */
public abstract class AbstractRemoteHandler implements RemoteHandler {

    private final String id;

    private final String permission;

    protected Set<RemoteSession> pushSessions = new HashSet<>();

    public AbstractRemoteHandler(String id, String permission)
    {
        this.id = id;
        this.permission = permission;
    }

    @Override
    public String getID()
    {
        return id;
    }

    @Override
    public String getPermission()
    {
        return permission;
    }

    public void register()
    {
        APIRegistry.remoteManager.registerHandler(this);
    }

    public static void checkPermission(RemoteSession session, String permission)
    {
        if (!APIRegistry.perms.checkUserPermission(session.getUserIdent(), permission))
            throw new PermissionException();
    }

    public static void error(String message)
    {
        throw new RemoteException(message);
    }

    protected synchronized void addPushSession(RemoteSession session)
    {
        pushSessions.add(session);
    }

    protected synchronized boolean hasPushSession(RemoteSession session)
    {
        return pushSessions.contains(session);
    }

    protected synchronized void removePushSession(RemoteSession session)
    {
        pushSessions.remove(session);
    }

    protected synchronized void push(RemoteResponse<?> response)
    {
        Iterator<RemoteSession> it = pushSessions.iterator();
        while (it.hasNext())
        {
            RemoteSession session = it.next();
            if (session.isClosed())
            {
                it.remove();
                continue;
            }
            session.trySendMessage(response);
        }
    }

    public static class PushRequest {

        public boolean enable;

        public PushRequest(boolean enable)
        {
            this.enable = enable;
        }
    }

}
