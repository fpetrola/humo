# Humo Language

Humo is a programming language with a tiny interpreter implementation and the smallest set of operations for an imperative programming language.

This is an experimental language that uses very few concepts to perform Turing complete computations.

Live demo: [Humo IDE](http://fpetrola.github.io/humo/humo-ide.html?file=http://fpetrola.github.io/humo/tests/1i.humo)

**Complete interpreter implementation code** (the following code executes any Humo program) :

``` Java
/*
 * Humo Language 
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package ar.net.fpetrola.humo;

import java.util.HashMap;
import java.util.Map;

public class HumoInterpreter
{
    protected Map<CharSequence, CharSequence> productions = new HashMap<CharSequence, CharSequence>();

    public int parse(StringBuilder sourcecode, int first)
    {
        int last = first, current = first;

        for (char currentChar; last < sourcecode.length() && (currentChar = sourcecode.charAt(last++)) != '}';)
        {
            if (currentChar == '{')
            {
                current = parse(sourcecode, last);
                productions.put(sourcecode.subSequence(first, last - 1), sourcecode.subSequence(last, current - 1));
                last = first = current;
            }
            
            CharSequence production = productions.get(sourcecode.subSequence(current, last));
            if (production != null)
            {
                StringBuilder value = new StringBuilder(production);
                parse(value, 0);
                sourcecode.replace(current, last, value.toString());
                last = current += value.length();
            }
        }

        return last;
    }
}
```

Coding example:
``` CartoCSS
//--------------------------------------------------------------------------{}
//---------------------- Humo Runtime Environment --------------------------{}
//--------------------------------------------------------------------------{}

[te{[t}mp]{emp]}
[exec]{[temp]}
//{[temp]}

run-main-class
{
    [exec]{#ref instance{main1}}
    [exec]{new Main}
    [exec]{$main1.execute}
}

#class{new}
#method{[do][llar]<*>instance.}
#field{[do][llar]<*>instance.}
#this.{[do][llar]<*>instance.}
#instance-name{#var instance}
#constructor-parameter {<***>}
#var {<***>}
#previous-value-of{<****>}
#parameter {<*>}
#new{<***>new}
#call{<**>}
#process-result{<**>}
#build-class{<**> instance{<***>instance}}
#last-result{<**> result}
#return{<***> result}
#ref{<*>}

<**{<*}
<*>{$}
[dollar]{$}
[do]{[do}
[llar]{llar]}

10[_x_]20{200}
30[_x_]40{1200}
[_x{[_}
_]{x_]}


#class TableBasedValueSolver
{
    #method solve
    {
        [exec]{#var first{#this.firstValue}}
        [exec]{#var second{#this.secondValue}}
        [exec]{#var combined{$first $operator $second}}

        [exec]{#return {#ref combined}}

        [exec]{#var first{#previous-value-of first}}
        [exec]{#var second{#previous-value-of second}}
        [exec]{#var operator{#previous-value-of operator}}
        [exec]{#var combined{#previous-value-of combined}}
    }

    #field operator {#parameter operator}
    #field firstValue {#parameter p1}
    #field secondValue {#parameter p2}
}



//--------------------------------------------------------------------------{}
//---------------------- Begin of application code -------------------------{}
//--------------------------------------------------------------------------{}


#class Rectangle
{
    #method getArea
    {
        [exec]{#constructor-parameter p1{#this.width}}
        [exec]{#constructor-parameter p2{#this.length}}
        [exec]{#constructor-parameter operator{[_x_]}}
        [exec]{#instance-name {solver}}
        [exec]{#new TableBasedValueSolver}
        [exec]{#call solver.solve}
        [exec]{#return {#last-result}}
    }

    #field width {#parameter width}
    #field length {#parameter length}
}

#class Main
{
    #method execute
    {
        [exec]{#this.m1}
        [exec]{#this.m2}
    }

    #method m1
    {
        [exec]{#constructor-parameter width{10}}
        [exec]{#constructor-parameter length{20}}
        [exec]{#instance-name {rect1}}
        [exec]{#new Rectangle}
        [exec]{#call rect1.getArea}
        [exec]{#last-result}
    }

    #method m2
    {
        [exec]{#constructor-parameter width{30}}
        [exec]{#constructor-parameter length{40}}
        [exec]{#instance-name {rect2}}
        [exec]{#new Rectangle}
        [exec]{#call rect2.getArea}
        [exec]{#last-result}
    }
}

[exec]{run-main-class}

//--------------------------------------------------------------------------{}
//----------------------- End of application code --------------------------{}
//--------------------------------------------------------------------------{}
```
