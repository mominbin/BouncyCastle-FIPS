package org.bouncycastle.crypto.general;

import java.io.IOException;
import java.math.BigInteger;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.crypto.InvalidSignatureException;
import org.bouncycastle.crypto.OutputVerifier;
import org.bouncycastle.crypto.Parameters;
import org.bouncycastle.crypto.internal.DSA;
import org.bouncycastle.crypto.internal.Digest;
import org.bouncycastle.crypto.internal.io.DigestOutputStream;

class DSAOutputVerifier<T extends Parameters>
    implements OutputVerifier<T>
{
    private final DSA dsa;
    private final Digest digest;
    private final T parameter;
    private final boolean reverse;

    DSAOutputVerifier(DSA dsa, Digest digest, T parameter)
    {
        this(dsa, digest, parameter, false);
    }

    DSAOutputVerifier(DSA dsa, Digest digest, T parameter, boolean reverse)
    {
        this.dsa = dsa;
        this.digest = digest;
        this.parameter = parameter;
        this.reverse = reverse;
    }

    public T getParameters()
    {
        return parameter;
    }

    public org.bouncycastle.crypto.UpdateOutputStream getVerifyingStream()
    {
        return new DigestOutputStream(digest);
    }

    public boolean isVerified(byte[] signature)
        throws InvalidSignatureException
    {
        try
        {
            BigInteger[] rs = decode(signature);

            byte[] m = new byte[digest.getDigestSize()];

            digest.doFinal(m, 0);

            return dsa.verifySignature(m, rs[0], rs[1]);
        }
        catch (IOException e)
        {
            throw new InvalidSignatureException("Unable to process signature: " + e.getMessage(), e);
        }
    }


    public BigInteger[] decode(
        byte[] encoding)
        throws IOException
    {
        BigInteger[] sig = new BigInteger[2];

        if (dsa instanceof EcGost3410Signer || dsa instanceof Gost3410Signer)
        {
            byte[] r = new byte[32];
            byte[] s = new byte[32];

            System.arraycopy(encoding, 0, s, 0, 32);
            System.arraycopy(encoding, 32, r, 0, 32);

            sig[0] = new BigInteger(1, r);
            sig[1] = new BigInteger(1, s);
        }
        else if (dsa instanceof DSTU4145Signer)
        {
            byte[] bytes = ((ASN1OctetString)ASN1OctetString.fromByteArray(encoding)).getOctets();
            byte[] r = new byte[bytes.length / 2];
            byte[] s = new byte[bytes.length / 2];

            if (reverse)
            {
                DSAUtils.reverseBytes(bytes);
            }

            System.arraycopy(bytes, 0, s, 0, bytes.length / 2);
            System.arraycopy(bytes, bytes.length / 2, r, 0, bytes.length / 2);

            sig = new BigInteger[2];
            sig[0] = new BigInteger(1, r);
            sig[1] = new BigInteger(1, s);
        }
        else
        {
            ASN1Sequence s = (ASN1Sequence)ASN1Primitive.fromByteArray(encoding);

            sig[0] = ASN1Integer.getInstance(s.getObjectAt(0)).getValue();
            sig[1] = ASN1Integer.getInstance(s.getObjectAt(1)).getValue();
        }

        return sig;
    }
}