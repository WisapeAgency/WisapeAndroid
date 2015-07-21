package com.wisape.android.logic;

import android.content.Context;
import android.test.ApplicationTestCase;

import com.wisape.android.WisapeApplication;
import com.wisape.android.database.StoryTemplateEntity;

/**
 * Created by tony on 2015/7/21.
 */
public class StoryLogicTest extends ApplicationTestCase<WisapeApplication> {
    private static final String TAG = "StoryLogicTest";

    public StoryLogicTest() {
        super(WisapeApplication.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
    }

    public void testListStoryTemplate(){
        Context context = getContext();
        StoryLogic logic = StoryLogic.instance();
        Object tag = new Object();
        StoryTemplateEntity[] templateArray = logic.listStoryTemplate(context, tag);
        assertNotNull(templateArray);
        int count = (null == templateArray ? 0 : templateArray.length);
        assertEquals(4, count);

        StoryTemplateEntity[] templateArrayFromDb = logic.listStoryTemplateLocal(context);
        assertNotNull(templateArrayFromDb);
        int countDb = (null == templateArrayFromDb ? 0 : templateArrayFromDb.length);
        assertEquals(count, countDb);
        assertEquals(templateArray[0].serverId, templateArrayFromDb[0].serverId);
    }


}
