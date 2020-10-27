package extracells.util;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

/**
 * This is a bride class, its needed bcs. the Methodes:
 * IMEMonitor IItemList<T> getAvailableItems(IItemList var1);
 * IMEInventory<StackType extends IAEStack> IItemList<StackType> getAvailableItems(IItemList<StackType> var1);
 *
 * have different identifiers, and therefore arent compatible to override with kotlin.
 */
public abstract class IMEMonitorBase implements IMEMonitor<IAEItemStack> {

    @Override
    public IItemList getAvailableItems(IItemList iItemList) {
        return null;
    }

    @Override
    public IAEItemStack injectItems(IAEItemStack iaeItemStack, Actionable actionable, BaseActionSource baseActionSource) {
        return null;
    }

    @Override
    public IAEItemStack extractItems(IAEItemStack iaeItemStack, Actionable actionable, BaseActionSource baseActionSource) {
        return null;
    }

    @Override
    public StorageChannel getChannel() {
        return null;
    }

    @Override
    public IItemList getStorageList() {
        return null;
    }

    @Override
    public void addListener(IMEMonitorHandlerReceiver imeMonitorHandlerReceiver, Object o) {

    }

    @Override
    public void removeListener(IMEMonitorHandlerReceiver imeMonitorHandlerReceiver) {

    }

    @Override
    public AccessRestriction getAccess() {
        return null;
    }

    @Override
    public boolean isPrioritized(IAEItemStack iaeItemStack) {
        return false;
    }

    @Override
    public boolean canAccept(IAEItemStack iaeItemStack) {
        return false;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(int i) {
        return false;
    }
}
