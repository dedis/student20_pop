package com.github.dedis.student20_pop.utility.security;

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Test;

public class HashTest {

  @Test
  public void hashTest() {
    assertNotNull(Hash.hash("Data to hash"));
  }

  @Test
  public void hashNullTest() {
    Assert.assertThrows(IllegalArgumentException.class, () -> Hash.hash());
    Assert.assertThrows(IllegalArgumentException.class, () -> Hash.hash((String) null));
    Assert.assertThrows(
        IllegalArgumentException.class, () -> Hash.hash((String) null, (String) null));
  }

  @Test
  public void hashEmptyStringTest() {
    Assert.assertThrows(IllegalArgumentException.class, () -> Hash.hash(""));
    Assert.assertThrows(IllegalArgumentException.class, () -> Hash.hash("", ""));
  }

  @Test
  public void hashObjectTest() {
    // Hashing : test 0 \fwa"fwa-fwa
    String expected = Hash.hash("[\"test\",\"0\",\"\\\\fwa\\\"fwa-fwa\"]");
    //    assertThat(Hash.hash("test", 0, "\\fwa\"fwa-fwa"), is(expected));
  }
}
