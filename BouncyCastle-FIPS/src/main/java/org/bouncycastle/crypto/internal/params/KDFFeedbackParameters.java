package org.bouncycastle.crypto.internal.params;

import org.bouncycastle.crypto.internal.DerivationParameters;
import org.bouncycastle.util.Arrays;

/**
 * Note that counter is only supported at the location presented in the
 * NIST SP 800-108 specification, not in the additional locations present
 * in the CAVP test vectors.
 */
public final class KDFFeedbackParameters
    implements DerivationParameters
{
    public static final int BEFORE_ITER = 0;
    public static final int AFTER_ITER = 1;
    public static final int AFTER_FIXED = 2;

    // could be any valid value, using 32, don't know why
    private static final int UNUSED_R = -1;

    private final int counterLocation;
    private final byte[] ki;
    private final byte[] iv;
    private final boolean useCounter;
    private final int r;
    private final byte[] fixedInputData;

    private KDFFeedbackParameters(int counterLocation, byte[] ki, byte[] iv, byte[] fixedInputData, int r, boolean useCounter)
    {
        if (ki == null)
        {
            throw new IllegalArgumentException("A KDF requires Ki (a seed) as input");
        }

        this.counterLocation = counterLocation;
        this.ki = Arrays.clone(ki);

        if (fixedInputData == null)
        {
            this.fixedInputData = new byte[0];
        }
        else
        {
            this.fixedInputData = Arrays.clone(fixedInputData);
        }

        this.r = r;

        if (iv == null)
        {
            this.iv = new byte[0];
        }
        else
        {
            this.iv = Arrays.clone(iv);
        }

        this.useCounter = useCounter;
    }

    public static KDFFeedbackParameters createWithCounter(
        int counterLocation, byte[] ki, final byte[] iv, byte[] fixedInputData, int r)
    {
        if (r != 8 && r != 16 && r != 24 && r != 32)
        {
            throw new IllegalArgumentException("Length of counter should be 8, 16, 24 or 32");
        }

        return new KDFFeedbackParameters(counterLocation, ki, iv, fixedInputData, r, true);
    }

    public static KDFFeedbackParameters createWithoutCounter(
        byte[] ki, final byte[] iv, byte[] fixedInputData)
    {
        return new KDFFeedbackParameters(BEFORE_ITER, ki, iv, fixedInputData, UNUSED_R, false);
    }

    public int getCounterLocation()
    {
        return counterLocation;
    }

    public byte[] getKI()
    {
        return ki;
    }

    public byte[] getIV()
    {
        return iv;
    }

    public boolean useCounter()
    {
        return useCounter;
    }

    public int getR()
    {
        return r;
    }

    public byte[] getFixedInputData()
    {
        return Arrays.clone(fixedInputData);
    }
}
