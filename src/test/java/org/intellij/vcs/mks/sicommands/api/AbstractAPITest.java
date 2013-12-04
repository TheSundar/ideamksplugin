package org.intellij.vcs.mks.sicommands.api;

import com.mks.api.response.Field;
import com.mks.api.response.Item;
import com.mks.api.response.ItemList;
import junit.framework.TestCase;
import org.intellij.vcs.mks.CommandExecutionListener;
import org.intellij.vcs.mks.MKSAPIHelper;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class AbstractAPITest extends TestCase {
    protected MKSAPIHelper apiHelper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        apiHelper = new MKSAPIHelper();
        apiHelper.initComponent();
    }

    @Override
    protected void tearDown() throws Exception {
        apiHelper.disposeComponent();
        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected MksCLIConfiguration getMksCLIConfiguration() {
        return new MksCLIConfiguration() {
            @NotNull
            public String getMksSiEncoding(final String command) {
                return "";
            }

            @NotNull
            public String getDatePattern() {
                return "MMM dd, yyyy - hh:mm a";
            }

            public CommandExecutionListener getCommandExecutionListener() {
                return CommandExecutionListener.IDLE;
            }

            @Override
            public boolean isMks2007() {
                return false;
            }
        };
    }

    protected void debug(String tab, Item item) {
        System.out.println(tab+item.getModelType());
        String newTab = "\t"+tab;
        for (Iterator it = item.getFields(); it.hasNext(); ) {
            Field field = (Field) it.next();
            String dataType = field.getDataType();
            if (Field.ITEM_TYPE.equals(dataType)) {
                System.out.print(newTab + field.getName() + " ");
                if ("si.Revision".equals(((Item) field.getValue()).getModelType())) {
                    System.out.println(field.getValueAsString());
                } else {
                    debug(newTab, (Item) field.getValue());
                }
            } else if (null == dataType || ViewSandboxAPITest.SIMPLE_TYPES.contains(dataType)) {
                System.out.println(newTab + field.getName() + " : " + field.getValue());
            } else if (Field.ITEM_LIST_TYPE.equals(dataType)) {
                ItemList itemList = (ItemList) field.getValue();
                System.out.print(newTab + field.getName() + " : List (" + itemList.size() + ") {");
                if (itemList.isEmpty()) {
                    System.out.println("}");
                } else {
                    System.out.println();
                    for (Iterator iterator = itemList.iterator(); iterator.hasNext(); ) {
                                        final Item next = (Item) iterator.next();
                                        debug(newTab + "\t", next);
                                    }
                    System.out.println(newTab + "}");
                }
            } else {
                System.out.println(newTab + field.getName() + " : " + field.getValue());
            }

        }
    }
}
