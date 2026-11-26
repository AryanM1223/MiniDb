
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Page {

    private static final int PAGE_SIZE = 4096;
    private static final int SLOT_SIZE = 5;

    private static final int PAGE_TYPE_OFFSET = 0;
    private static final int SLOT_COUNT_OFFSET = 1;
    private static final int FREE_START_OFFSET = 3;
    private static final int FREE_END_OFFSET = 5;
    private static final int NEXT_OFFSET = 7;
    private static final int PAGE_ID_OFFSET = 11;

    private final byte[] data;
    private final ByteBuffer buffer;

    public Page(byte pageType, int pageId) {
        this.data = new byte[PAGE_SIZE];
        this.buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

        init(pageType, pageId);
    }

    public Page(byte[] data) {
        if (data == null || data.length != PAGE_SIZE) {
            throw new IllegalArgumentException("Invalid page data");
        }

        this.data = data;
        this.buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
    }

    private void init(byte pageType, int pageId) {
        setPageType(pageType);
        setSlotCount((short) 0);
        setFreeStart((short) 15);
        setFreeEnd((short) PAGE_SIZE);
        setNextPageId(-1);
        setPageId(pageId);
    }


    public int getPageId() {
        return buffer.getInt(PAGE_ID_OFFSET);
    }

    public byte getPageType() {
        return buffer.get(PAGE_TYPE_OFFSET);
    }

    public short getSlotCount() {
        return buffer.getShort(SLOT_COUNT_OFFSET);
    }

    public short getFreeStart() {
        return buffer.getShort(FREE_START_OFFSET);
    }

    public short getFreeEnd() {
        return buffer.getShort(FREE_END_OFFSET);
    }

    public int getNextPageId() {
        return buffer.getInt(NEXT_OFFSET);
    }

    public byte[] getData() {
        return data;
    }


    public void setPageId(int pageId) {
        buffer.putInt(PAGE_ID_OFFSET, pageId);
    }

    public void setPageType(byte pageType) {
        buffer.put(PAGE_TYPE_OFFSET, pageType);
    }

    public void setSlotCount(short slotCount) {
        buffer.putShort(SLOT_COUNT_OFFSET, slotCount);
    }

    public void setFreeStart(short freeStart) {
        buffer.putShort(FREE_START_OFFSET, freeStart);
    }

    public void setFreeEnd(short freeEnd) {
        buffer.putShort(FREE_END_OFFSET, freeEnd);
    }

    public void setNextPageId(int nextPageId) {
        buffer.putInt(NEXT_OFFSET, nextPageId);
    }


    public int getFreeSpace() {
        return getFreeEnd() - getFreeStart();
    }


    public int insertRecord(byte[] record) {

        if (getPageType() == 0 || record == null) {
            return -1;
        }

        int freeSpace = getFreeSpace();

        if (freeSpace < record.length + SLOT_SIZE) {
            return -1;
        }

        short free = (short) (getFreeEnd() - record.length);

        for (int i = 0; i < record.length; i++) {
            data[free + i] = record[i];
        }

        int slotPosition = 15 + (getSlotCount() * SLOT_SIZE);

        buffer.putShort(slotPosition, free);
        buffer.putShort(slotPosition + 2, (short) record.length);
        buffer.put(slotPosition + 4, (byte) 1);

        setFreeEnd(free);

        short freeS = (short) (getFreeStart() + SLOT_SIZE);
        setFreeStart(freeS);

        short slot = (short) (getSlotCount() + 1);
        setSlotCount(slot);

        return slot - 1;
    }


    public byte[] getRecord(int slotId) {

        if (slotId < 0 || slotId >= getSlotCount()) {
            return null;
        }

        int slotPosition = 15 + (slotId * SLOT_SIZE);

        byte status = buffer.get(slotPosition + 4);

        if (status == 0) {
            return null;
        }

        short offset = buffer.getShort(slotPosition);
        short length = buffer.getShort(slotPosition + 2);

        byte[] record = new byte[length];

        System.arraycopy(data, offset, record, 0, length);

        return record;
    }

    public boolean deleteRecord(int slotId) {

        if (slotId < 0 || slotId >= getSlotCount()) {
            return false;
        }

        int slotPosition = 15 + (slotId * SLOT_SIZE);

        byte status = buffer.get(slotPosition + 4);

        if (status == 0) {
            return false;
        }

        buffer.put(slotPosition + 4, (byte) 0);

        return true;
    }
    public void compact() {

        byte[] newData = new byte[PAGE_SIZE];

        System.arraycopy(data, 0, newData, 0, getFreeStart());

        short newFreeEnd = (short) PAGE_SIZE;

        for (int i = 0; i < getSlotCount(); i++) {

            int slotPosition = 15 + (i * SLOT_SIZE);

            byte status = buffer.get(slotPosition + 4);

            if (status == 0) {
                continue;
            }

            short offset = buffer.getShort(slotPosition);
            short length = buffer.getShort(slotPosition + 2);

            newFreeEnd -= length;

            System.arraycopy(data, offset, newData, newFreeEnd, length);

            ByteBuffer newBuffer = ByteBuffer.wrap(newData)
                    .order(ByteOrder.BIG_ENDIAN);

            newBuffer.putShort(slotPosition, newFreeEnd);
        }

        ByteBuffer newBuffer = ByteBuffer.wrap(newData)
                .order(ByteOrder.BIG_ENDIAN);

        newBuffer.putShort(FREE_END_OFFSET, newFreeEnd);

        System.arraycopy(newData, 0, data, 0, PAGE_SIZE);
    }
}
