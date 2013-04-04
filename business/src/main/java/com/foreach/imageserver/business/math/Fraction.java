package com.foreach.imageserver.business.math;

public class Fraction implements Comparable  {

    private final int p;
    private final int q;

    public static final Fraction ONE = new Fraction( 1, 1 );

    public static final Fraction UNDEFINED = new Fraction( 0, 0 );

    public Fraction( int p, int q )
    {
        int gcd = gcd( q, p );

        // if q == 0, we normalize to 0/0 instead of 1/0

        this.p = ( q == 0 ) ? 0 : ( ( q > 0 )? p : -p ) / gcd;
        this.q = ( q == 0 ) ? 0 : ( ( q > 0 )? q : -q ) / gcd;
    }

    public final int getNumerator()
    {
        return p;
    }

    public final int getDenominator()
    {
        return q;
    }

    public final boolean isUndefined()
    {
        return this.equals( UNDEFINED );
    }

    public final int compareTo(Object o)
    {
        if ( !( o instanceof Fraction ) ) {
            throw new ClassCastException( "Can't compare with non-fractions." );
        }

        Fraction other = (Fraction) o;

        if( isUndefined() || other.isUndefined() ) {

            // Works because we know q is always postive

            return q - other.q;
        }

        long p1q2 = (long) p * other.q;
        long p2q1 = (long) q * other.p;

        if( p1q2 > p2q1 ) {
            return 1;
        }
        if( p1q2 < p2q1 ) {
            return -1;
        }
        return 0;
    }

    public final boolean equals( Object o )
    {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        Fraction other =  (Fraction) o;

        return ( p == other.p ) && ( q == other.q );
    }

    private static boolean validInteger( long l )
    {
        return ( ( l <= Integer.MAX_VALUE ) && ( l >= Integer.MIN_VALUE ) );
    }

    private static Fraction boundsCheck( long p, long q )
    {
        if ( ! validInteger( p ) || ! validInteger( q ) ) {
            throw new ArithmeticException( "The result of the operation can not be accurately represented as a fraction" );
        }
        return new Fraction( (int) p, (int) q );
    }

	public final Fraction chs()
	{
	    return new Fraction( -p, q );
	}

	public final Fraction abs()
	{
	    return new Fraction( ( p < 0)? -p : p, q );
	}

    public final Fraction multiplyWith( Fraction multiplicator )
    {
        long numerator = (long) p * multiplicator.p;
        long denominator = (long) q * multiplicator.q;
        return boundsCheck( numerator, denominator );
    }

    public final Fraction divideBy( Fraction dividor )
    {
        long numerator = (long) p * dividor.q;
        long denominator = (long) q * dividor.p;
        return boundsCheck( numerator, denominator );
    }

	public final Fraction addInteger( int addend )
	{
	    long numerator = (long) q * addend + p;
	    long denominator = (long) q;
	    return boundsCheck( numerator, denominator );
	}

	public final boolean isNegative()
	{
		return ( p < 0 );
	}

	// returns true if the revceiver lies in the interval [(1-t)g, (1+t)g]
	public final boolean withinTolerance( Fraction g, Fraction t )
	{
		Fraction upperBound = ( g.isNegative()? t.chs().addInteger( 1 ) : t.addInteger( 1 ) ).multiplyWith( g );
		Fraction lowerBound = ( g.isNegative()? t.addInteger( 1 ) : t.chs().addInteger( 1 ) ).multiplyWith( g );

		return ( lowerBound.compareTo( this ) <= 0 ) && (upperBound.compareTo( this ) >= 0 );
	}

    /*
        TODO: pjs (20110707)

        It would be nice to have subsequent scale and descale operation stabilise,

        i.e.

        f.descale( f.scale( n ) ) = n' may differ from n, but
        f.descale( f.scale( n' ) ) should be n'
     */

    public final int scale( int n )
    {
        return (int) ( ( (long) p * n ) / q );
    }

    public final int deScale( int n )
    {
        return (int) ( ( (long) q * n ) / p );
    }

    @Override
    public final String toString()
    {
        if( this.equals( Fraction.UNDEFINED ) ) {
	        return "undefined";
        }

	    return new StringBuilder()
                .append( p )
                .append( '/')
                .append( q )
                .toString();
    }

    // The default toString doesn't play nice with urls
    public final String getStringForUrl()
    {
        return new StringBuilder()
                .append( p )
                .append( 'x' )
                .append( q )
                .toString();
    }

    private int gcd( int a, int b)
    {
        int na = ( a < 0 )? -a : a;
        int nb = ( b < 0 )? -b : b;

        // If one number is zero, we return the other

        if( na == 0 ) {
            return nb;
        }
        if( nb == 0 ) {
            return na;
        }


        while (nb != 0) {
            int temp = nb;
            nb = na % nb;
            na = temp;
        }
        return na;
    }

    @Override
    public final int hashCode()
    {
        return 10007 * p + q;
    }

    public final String getLossyRepresentation()
    {
        float f = ( 1.0f * p ) / q;
        return Float.toString( f );
    }

    // Parses both strings generated by toString() and getStringForUrl()
    public static Fraction parseString( String s )
    {
        int f = s.indexOf( 'x' );

        if( f == -1 ) {
            f = s.indexOf( '/' );
        }
        if( f == -1 ) {
            throw new NumberFormatException( "Not a valid Fraction: "+s );
        }

        int p = Integer.parseInt( s.substring( 0, f), 10 );
        int q = Integer.parseInt( s.substring( f + 1 ), 10 );

        return new Fraction( p, q );
    }
}
