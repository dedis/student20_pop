package com.github.dedis.student20_pop.model.network.method.message;

import com.github.dedis.student20_pop.model.network.method.message.data.lao.CreateLao;
import com.github.dedis.student20_pop.utility.network.IdGenerator;
import com.github.dedis.student20_pop.utility.security.Hash;
import com.google.android.gms.common.util.Hex;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

public class MessageGeneralTest {

    private final String organizer = "Z3DYtBxooGs6KxOAqCWD3ihR8M6ZPBjAmWp_w5VBaws=";
    private final long creation = 1623825071;
    private final String name = "LAO";
    private final ArrayList<PublicKeySignaturePair> witnessSignatures = new ArrayList<>();
    private final byte[] messageId = "y12RD6CXaDpULqoqENysmXzLVXhbQvHBKj0pEY90ZlQ=".getBytes(StandardCharsets.UTF_8);
    private final byte[] sender = new byte[] {103, 112, -40, -76, 28, 104, -96, 107, 58, 43, 19, -128, -88, 37, -125, -34, 40, 81, -16, -50, -103, 60, 24, -64, -103, 106, 127, -61, -107, 65, 107, 11};
    private final byte[] dataBuf = new byte[] {123, 34, 99, 114, 101, 97, 116, 105, 111, 110, 34, 58, 49, 54, 50, 51, 56, 50, 53, 48, 55, 49, 44, 34, 105, 100, 34, 58, 34, 78, 79, 102, 57, 71, 76, 102, 74, 89, 53, 99, 85, 82, 100, 105, 74, 109, 105, 108, 89, 114, 115, 89, 79, 90, 97, 107, 45, 107, 95, 55, 118, 50, 117, 122, 52, 108, 108, 67, 83, 69, 49, 77, 61, 34, 44, 34, 110, 97, 109, 101, 34, 58, 34, 76, 65, 79, 34, 44, 34, 111, 114, 103, 97, 110, 105, 122, 101, 114, 34, 58, 34, 90, 51, 68, 89, 116, 66, 120, 111, 111, 71, 115, 54, 75, 120, 79, 65, 113, 67, 87, 68, 51, 105, 104, 82, 56, 77, 54, 90, 80, 66, 106, 65, 109, 87, 112, 95, 119, 53, 86, 66, 97, 119, 115, 61, 34, 44, 34, 119, 105, 116, 110, 101, 115, 115, 101, 115, 34, 58, 91, 93, 44, 34, 111, 98, 106, 101, 99, 116, 34, 58, 34, 108, 97, 111, 34, 44, 34, 97, 99, 116, 105, 111, 110, 34, 58, 34, 99, 114, 101, 97, 116, 101, 34, 125};
    private final byte[] signature = new byte[] {-55, 121, 102, -88, -17, 87, -125, -87, 93, -127, -56, -125, -14, -61, 56, 117, 35, -113, -82, 57, 107, 85, 100, -30, 43, 69, 22, 42, -25, 66, -70, -64, 20, -31, -32, -112, -78, 115, 9, 13, -37, 59, -29, 45, 12, 54, 71, -73, 119, 89, 119, 106, 24, -115, 67, 103, -91, 29, -122, 83, -38, 101, 101, 11};
    private final CreateLao data = new CreateLao(IdGenerator.generateLaoId(organizer, creation, name), name, creation, organizer, new ArrayList<>());
    private final MessageGeneral messageGeneral = new MessageGeneral(sender, dataBuf, data, signature, messageId, witnessSignatures);

    @Test
    public void messageGeneralWithNonBase64URLIdTest() {
        assertThrows(IllegalArgumentException.class, () -> new MessageGeneral(sender, dataBuf, data, signature, new byte[] {1, 2, 3}, witnessSignatures));
    }

    @Test
    public void messageIdGeneration() {
        assertThat(messageGeneral.getMessageId(), is(Hash.hash(Base64.getUrlEncoder().encodeToString(this.dataBuf), Base64.getUrlEncoder().encodeToString(this.signature))));
        assertThat(Hex.bytesToStringUppercase(Base64.getUrlDecoder().decode(messageGeneral.getMessageId())), is("CB5D910FA097683A542EAA2A10DCAC997CCB55785B42F1C12A3D29118F746654"));
    }
}
