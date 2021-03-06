package com.github.dedis.student20_pop.model.network.method.message.data.lao;

import com.github.dedis.student20_pop.model.network.method.message.PublicKeySignaturePair;
import com.github.dedis.student20_pop.utility.network.IdGenerator;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

public class StateLaoTest {
    private final String name = " Lao name";
    private final long creation = 0x10;
    private final long lastModified = 0x999;
    private final String organizer = "Organizer Id";
    private final String modificationId = " modification id";
    private final Set<String> witnesses = new HashSet<>(Arrays.asList("0x3434", "0x4747"));
    private final String id = IdGenerator.generateLaoId(organizer, creation, name);
    private final List<PublicKeySignaturePair> modificationSignatures = Arrays.asList(new PublicKeySignaturePair(
            new byte[10], new byte[10]));
    private final StateLao stateLao = new StateLao(id, name, creation, lastModified, organizer, modificationId, witnesses, modificationSignatures);

    @Test
    public void wrongIdTest() {
        assertThrows(IllegalArgumentException.class, () -> new StateLao("wrong id", name, creation, lastModified, organizer, modificationId, witnesses, modificationSignatures));
    }

    @Test
    public void getIdTest() {
        assertThat(stateLao.getId(), is(id));
    }

    @Test
    public void getNameTest() {
        assertThat(stateLao.getName(), is(name));
    }

    @Test
    public void getCreationTest() {
        assertThat(stateLao.getCreation(), is(creation));
    }

    @Test
    public void getLastModifiedTest() {
        assertThat(stateLao.getLastModified(), is(lastModified));
    }

    @Test
    public void getOrganizerTest() {
        assertThat(stateLao.getOrganizer(), is(organizer));
    }

    @Test
    public void getWitnessesTest() {
        assertThat(stateLao.getWitnesses(), is(witnesses));
    }

    @Test
    public void getModificationIdTest() {
        assertThat(stateLao.getModificationId(), is(modificationId));
    }

    @Test
    public void getModificationIdSignaturesTest() {
        assertThat(stateLao.getModificationId(), is(modificationId));
    }

    @Test
    public void isEqualTest() {
        assertEquals(stateLao, new StateLao(id, name, creation, lastModified, organizer, modificationId, witnesses, modificationSignatures));
        // The modification id isn't taken into account to know if they are equal
        assertEquals(stateLao, new StateLao(id, name, creation, lastModified, organizer, "random", witnesses, modificationSignatures));
        // same goes for modification signatures
        assertEquals(stateLao, new StateLao(id, name, creation, lastModified, organizer, modificationId, witnesses, null));
        String random = " random string";
        String newId = IdGenerator.generateLaoId(organizer, creation, random);
        assertNotEquals(stateLao, new StateLao(newId, random, creation, lastModified, organizer, modificationId, witnesses, modificationSignatures));
        newId = IdGenerator.generateLaoId(random, creation, name);
        assertNotEquals(stateLao, new StateLao(newId, name, creation, lastModified, random, modificationId, witnesses, modificationSignatures));
        newId = IdGenerator.generateLaoId(organizer, 99, name);
        assertNotEquals(stateLao, new StateLao(newId, name, 99, lastModified, organizer, modificationId, witnesses, modificationSignatures));
        assertNotEquals(stateLao, new StateLao(id, name, creation, 1000, organizer, modificationId, witnesses, modificationSignatures));
        assertNotEquals(stateLao, new StateLao(id, name, creation, lastModified, organizer, modificationId, new HashSet<>(Arrays.asList("0x3434")), modificationSignatures));
    }

}
