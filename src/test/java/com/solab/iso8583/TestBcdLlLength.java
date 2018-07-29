package com.solab.iso8583;

import com.solab.iso8583.util.HexCodec;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * These are tests for bcd length of ll-fields
 *
 * @author Sergey Zyazyulkin
 */
public class TestBcdLlLength {

    private static final String LLVAR = "1234567890";

    private static final String LLLVAR =
            "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";

    private static final byte[] LLBIN = new byte[] {1};
    private static final byte[] LLLBIN = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};

    private static final String MESSAGE = "30323030" + // 0200
            "37383030303030303030303030303030" + // bitmap
            "1031323334353637383930" + // LLVAR
            "0100" + // LLLVAR
            "31323334353637383930" +
            "31323334353637383930" +
            "31323334353637383930" +
            "31323334353637383930" +
            "31323334353637383930" +
            "31323334353637383930" +
            "31323334353637383930" +
            "31323334353637383930" +
            "31323334353637383930" +
            "31323334353637383930" +
            "0101" + // LLBIN
            "30313001020304050607080900"; // LLLBIN

    private static final byte[] MESSAGE_BYTES = HexCodec.hexDecode(MESSAGE);

    private MessageFactory<IsoMessage> mf;

    @Before
    public void init() throws IOException {
        mf = new MessageFactory<>();
        mf.setConfigPath("bcd.xml");
        mf.setForceSecondaryBitmap(false);
    }

    @Test
    public void testEncoding() {
        final IsoMessage msg = createMessage();
        Assert.assertEquals(MESSAGE, toHex(msg));
    }

    @Test
    public void testDecoding() throws Exception {
        final IsoMessage msg = mf.parseMessage(MESSAGE_BYTES, 0);
        validateMessage(msg);
    }

    @Test
    public void testEncodingDecoding() throws Exception {
        final IsoMessage msg = createMessage();
        final byte[] msgBytes = msg.writeData();
        final IsoMessage decoded = mf.parseMessage(msgBytes, 0);
        validateMessage(decoded);
    }

    @Test
    public void testDecodingEncoding() throws Exception {
        final IsoMessage msg = mf.parseMessage(MESSAGE_BYTES, 0);
        final byte[] msgBytes = msg.writeData();
        Assert.assertArrayEquals(MESSAGE_BYTES, msgBytes);
    }

    private @NotNull IsoMessage createMessage() {
        final IsoMessage msg = mf.newMessage(0x200);
        msg.setField(2, new IsoValue<>(IsoType.LLVAR, LLVAR));
        msg.setField(3, new IsoValue<>(IsoType.LLLVAR, LLLVAR));
        msg.setField(4, new IsoValue<>(IsoType.LLBIN, LLBIN));
        msg.setField(5, new IsoValue<>(IsoType.LLLBIN, LLLBIN));
        return msg;
    }

    private void validateMessage(final @NotNull IsoMessage msg) {
        final String parsedLlVar = msg.getObjectValue(2);
        final String parsedLllVar = msg.getObjectValue(3);
        final byte[] parsedLlBin = msg.getObjectValue(4);
        final byte[] parsedLllBin = msg.getObjectValue(5);
        Assert.assertEquals(LLVAR, parsedLlVar);
        Assert.assertEquals(LLLVAR, parsedLllVar);
        Assert.assertArrayEquals(LLBIN, parsedLlBin);
        Assert.assertArrayEquals(LLLBIN, parsedLllBin);
    }

    private @NotNull String toHex(final @NotNull IsoMessage isoMessage) {
        final byte[] data = isoMessage.writeData();
        return HexCodec.hexEncode(data, 0, data.length);
    }
}
