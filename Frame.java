public class Frame {
    private final int pageId;
    private Page page;
    private int pinCount;
    private boolean dirty;

    public Frame(int pageId,Page page,int pinCount,boolean dirty){
        this.pinCount = pinCount;
        this.page = page;
        this.dirty=dirty;
        this.pageId = pageId;
    }

    public void reset(int pageId){
        page = null;
        pageId = -1;
        pinCount = 0;
        dirty = false;
    }
}
