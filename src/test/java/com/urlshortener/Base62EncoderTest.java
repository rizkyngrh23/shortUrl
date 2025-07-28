package com.urlshortener;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Base62EncoderTest {

    @Test
    void testEncode() {
        assertEquals("0", Base62Encoder.encode(0));
        assertEquals("1", Base62Encoder.encode(1));
        assertEquals("Z", Base62Encoder.encode(35));
        assertEquals("z", Base62Encoder.encode(61));
        assertEquals("10", Base62Encoder.encode(62));
        assertEquals("1000", Base62Encoder.encode(238328));
    }

    @Test
    void testDecode() {
        assertEquals(0, Base62Encoder.decode("0"));
        assertEquals(1, Base62Encoder.decode("1"));
        assertEquals(35, Base62Encoder.decode("Z"));
        assertEquals(61, Base62Encoder.decode("z"));
        assertEquals(62, Base62Encoder.decode("10"));
        assertEquals(238328, Base62Encoder.decode("1000"));
    }

    @Test
    void testEncodeDecodeRoundTrip() {
        for (long i = 0; i < 10000; i++) {
            String encoded = Base62Encoder.encode(i);
            long decoded = Base62Encoder.decode(encoded);
            assertEquals(i, decoded, "Round trip failed for: " + i);
        }
    }

    @Test
    void testGenerateRandomCode() {
        String code1 = Base62Encoder.generateRandomCode(6);
        String code2 = Base62Encoder.generateRandomCode(6);
        
        assertEquals(6, code1.length());
        assertEquals(6, code2.length());
        assertNotEquals(code1, code2);
        
        for (char c : code1.toCharArray()) {
            assertTrue(Character.isLetterOrDigit(c));
        }
    }

    @Test
    void testDecodeInvalidCharacter() {
        assertThrows(IllegalArgumentException.class, () -> {
            Base62Encoder.decode("@");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            Base62Encoder.decode("abc#def");
        });
    }
}
