# Humo Language

Humo is a programming language with a tiny interpreter implementation and the smallest set of operations for an imperative programming language.

This is an experimental language that uses very few concepts to perform Turing complete computations.

Live demo: [Humo IDE](http://fpetrola.github.io/humo/humo-ide.html?file=https://raw.githubusercontent.com/fpetrola/humo/master/humo/tests/example2.humo)

<img src="humo3.png" alt="HumoVM"	title="Humo VM in QR" style="width: 300px; image-rendering: auto;  image-rendering: crisp-edges;  image-rendering: pixelated;" />

# Humo: A Dynamic String-Rewriting System

**Humo** is a computational model proven to be **Turing Complete**, built entirely upon **string rewriting** and **recursive substitution**. It differs fundamentally from traditional interpreters by operating only on a single, continuous string of text and possessing no internal keywords or reserved memory structures.

## Theoretical Foundation

Formally, Humo functions as a **Dynamic Semi-Thue System** where the set of substitution rules evolves during execution.

### The Axiom: `pattern{replacement}`

The engine recognizes only one primitive operation: the **Rule Definition**.

1.  **Definition Phase:** When the engine encounters `pattern{replacement}`, it registers that the string `pattern` maps to `replacement`.
2.  **Expansion Phase:** When the engine encounters `pattern` later, it replaces it with `replacement`.

The core principle is **recursive expansion**: the output of a substitution is immediately re-scanned. This capability is what allows the program to **dynamically modify its own rule set** (meta-programming) and create complex control flow. Crucially, all standard syntax (e.g., `[run]`, `$`) is **user-defined convention**, not part of the engine itself.

---

## Turing Completeness
Humo has been empirically proven to be Turing Complete.

By utilizing the mechanisms described above—specifically the infinite tape simulation via dynamic variables (tape_n), conditional state transitions (RULE_q_sym), and recursive control flow—we have successfully implemented a full Universal Turing Machine within Humo.

Proof of Concept: A functional implementation of a Decimal-to-Binary Converter was built using Humo. This implementation replicates the state table of a standard Turing Machine, handling:

Infinite tape traversal (Left/Right movements).

Symbol reading and writing via string concatenation (tape_ + position).

State changes via dynamic rule dispatch (RULE_ + state + symbol).

This demonstrates that despite having only a single "instruction" (text substitution), Humo is capable of computing any algorithm that a Turing Machine can run.

---

**Complete interpreter implementation code** (the following code executes any Humo program) :

``` Java
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
                productions.put(sourcecode.subSequence(first, last - 1), sourcecode.subSequence(last, current));
                last = first = ++current;
            }
            else
            {
                CharSequence production = productions.get(sourcecode.subSequence(current, last));
                if (production != null)
                {
                    StringBuilder value = new StringBuilder(production);
                    parse(value, 0);
                    sourcecode.replace(current, last, value.toString());
                    last = current += value.length();
                }
            }
        }

        return last - 1;
    }
}
```

Infinite Tape Turing Machine: Dec2Bin converter

```css
[comm{[com]ent}{ment}]
[comment] {------------------------------------------------ Begin Humo Runtime -------------------------------------------------------------}

[run]{[comment]}
[vari{[var]able}{iable}]
[exec{[exe]ute}{cute}]

#class{new}
#this.{[execute]<*>instance:name.}
#method:{#this.}
#property:[#this.]
#property->{[variable]$instance:name.}

[v]{<***>}
<--{<->}
<->{@}
<**>{#*}
<*>{$}
<..{<..}
<-.>{}

@[{{}]

#begin-construction {
[run]{<--><<..>vari{<*>}<...able]>}>}}
[run]{<--><<..>exec[]{<...}ute]{$}}
}
#end-construction {
[run]{<***>instance:name{<***>instance:name}}
[run]{<--->[vari{[var]<....>able]{iable}]}
[run]{<--->[exec{exe}<....>ute]{cute}]}
}

[comment] {-------------------------------- End Humo Runtime
[comment] {-------------------------------------------------------------}

[comment] {---------------- REGLAS DE LA MAQUINA DE TURING (DEC -> BIN) ----------------}
[comment] { Formato: RULE_Estado_Simbolo -> body que setea nextState, writeVal, moveDir }
[comment] { ESTADO qinit: Mover al final del numero }
$RULE_qinit_0{ [v] nextState{qinit} [v] writeVal{0} [v] moveDir{R} }
$RULE_qinit_1{ [v] nextState{qinit} [v] writeVal{1} [v] moveDir{R} }
$RULE_qinit_2{ [v] nextState{qinit} [v] writeVal{2} [v] moveDir{R} }
$RULE_qinit_3{ [v] nextState{qinit} [v] writeVal{3} [v] moveDir{R} }
$RULE_qinit_4{ [v] nextState{qinit} [v] writeVal{4} [v] moveDir{R} }
$RULE_qinit_5{ [v] nextState{qinit} [v] writeVal{5} [v] moveDir{R} }
$RULE_qinit_6{ [v] nextState{qinit} [v] writeVal{6} [v] moveDir{R} }
$RULE_qinit_7{ [v] nextState{qinit} [v] writeVal{7} [v] moveDir{R} }
$RULE_qinit_8{ [v] nextState{qinit} [v] writeVal{8} [v] moveDir{R} }
$RULE_qinit_9{ [v] nextState{qinit} [v] writeVal{9} [v] moveDir{R} }
$RULE_qinit_{ [v] nextState{halve} [v] writeVal{0} [v] moveDir{L} }

[comment] { ESTADO halve: Dividir digito actual por 2 }
$RULE_halve_0{ [v] nextState{halve} [v] writeVal{0} [v] moveDir{L} }
$RULE_halve_1{ [v] nextState{addHalf} [v] writeVal{0} [v] moveDir{R} }
$RULE_halve_2{ [v] nextState{halve} [v] writeVal{1} [v] moveDir{L} }
$RULE_halve_3{ [v] nextState{addHalf} [v] writeVal{1} [v] moveDir{R} }
$RULE_halve_4{ [v] nextState{halve} [v] writeVal{2} [v] moveDir{L} }
$RULE_halve_5{ [v] nextState{addHalf} [v] writeVal{2} [v] moveDir{R} }
$RULE_halve_6{ [v] nextState{halve} [v] writeVal{3} [v] moveDir{L} }
$RULE_halve_7{ [v] nextState{addHalf} [v] writeVal{3} [v] moveDir{R} }
$RULE_halve_8{ [v] nextState{halve} [v] writeVal{4} [v] moveDir{L} }
$RULE_halve_9{ [v] nextState{addHalf} [v] writeVal{4} [v] moveDir{R} }
$RULE_halve_{ [v] nextState{removezero} [v] writeVal{_} [v] moveDir{R} }

[comment] { ESTADO addHalf: Sumar 5 al digito actual (carry de division impar) }
$RULE_addHalf_0{ [v] nextState{jump} [v] writeVal{5} [v] moveDir{L} }
$RULE_addHalf_1{ [v] nextState{jump} [v] writeVal{6} [v] moveDir{L} }
$RULE_addHalf_2{ [v] nextState{jump} [v] writeVal{7} [v] moveDir{L} }
$RULE_addHalf_3{ [v] nextState{jump} [v] writeVal{8} [v] moveDir{L} }
$RULE_addHalf_4{ [v] nextState{jump} [v] writeVal{9} [v] moveDir{L} }

[comment] { ESTADO jump: Volver atras para seguir dividiendo }
$RULE_jump_0{ [v] nextState{halve} [v] writeVal{0} [v] moveDir{L} }
$RULE_jump_1{ [v] nextState{halve} [v] writeVal{1} [v] moveDir{L} }
$RULE_jump_2{ [v] nextState{halve} [v] writeVal{2} [v] moveDir{L} }
$RULE_jump_3{ [v] nextState{halve} [v] writeVal{3} [v] moveDir{L} }
$RULE_jump_4{ [v] nextState{halve} [v] writeVal{4} [v] moveDir{L} }

[comment] { ESTADO removezero: Limpiar ceros a la izquierda }
$RULE_removezero_0{ [v] nextState{removezero} [v] writeVal{_} [v] moveDir{R} }
$RULE_removezero_1{ [v] nextState{goBack} [v] writeVal{1} [v] moveDir{R} }
$RULE_removezero_2{ [v] nextState{goBack} [v] writeVal{2} [v] moveDir{R} }
$RULE_removezero_3{ [v] nextState{goBack} [v] writeVal{3} [v] moveDir{R} }
$RULE_removezero_4{ [v] nextState{goBack} [v] writeVal{4} [v] moveDir{R} }
$RULE_removezero_5{ [v] nextState{goBack} [v] writeVal{5} [v] moveDir{R} }
$RULE_removezero_6{ [v] nextState{goBack} [v] writeVal{6} [v] moveDir{R} }
$RULE_removezero_7{ [v] nextState{goBack} [v] writeVal{7} [v] moveDir{R} }
$RULE_removezero_8{ [v] nextState{goBack} [v] writeVal{8} [v] moveDir{R} }
$RULE_removezero_9{ [v] nextState{goBack} [v] writeVal{9} [v] moveDir{R} }
$RULE_removezero_{ [v] nextState{qfin} [v] writeVal{_} [v] moveDir{R} }

[comment] { ESTADO goBack: Mover a la derecha hasta el final del numero }
$RULE_goBack_0{ [v] nextState{goBack} [v] writeVal{0} [v] moveDir{R} }
$RULE_goBack_1{ [v] nextState{goBack} [v] writeVal{1} [v] moveDir{R} }
$RULE_goBack_2{ [v] nextState{goBack} [v] writeVal{2} [v] moveDir{R} }
$RULE_goBack_3{ [v] nextState{goBack} [v] writeVal{3} [v] moveDir{R} }
$RULE_goBack_4{ [v] nextState{goBack} [v] writeVal{4} [v] moveDir{R} }
$RULE_goBack_5{ [v] nextState{goBack} [v] writeVal{5} [v] moveDir{R} }
$RULE_goBack_6{ [v] nextState{goBack} [v] writeVal{6} [v] moveDir{R} }
$RULE_goBack_7{ [v] nextState{goBack} [v] writeVal{7} [v] moveDir{R} }
$RULE_goBack_8{ [v] nextState{goBack} [v] writeVal{8} [v] moveDir{R} }
$RULE_goBack_9{ [v] nextState{goBack} [v] writeVal{9} [v] moveDir{R} }
$RULE_goBack_{ [v] nextState{rest} [v] writeVal{_} [v] moveDir{L} }

[comment] { ESTADO rest: Determinar bit menos significativo }
$RULE_rest_0{ [v] nextState{rest0} [v] writeVal{_} [v] moveDir{R} }
$RULE_rest_5{ [v] nextState{rest1} [v] writeVal{_} [v] moveDir{R} }

[comment] { ESTADOS rest0/rest1: Avanzar al resultado binario }
$RULE_rest0_{ [v] nextState{setrest0} [v] writeVal{_} [v] moveDir{R} }
$RULE_rest1_{ [v] nextState{setrest1} [v] writeVal{_} [v] moveDir{R} }

[comment] { ESTADOS setrest0/setrest1: Moverse al final del resultado binario y escribir bit }
$RULE_setrest0_0{ [v] nextState{setrest0} [v] writeVal{0} [v] moveDir{R} }
$RULE_setrest0_1{ [v] nextState{setrest0} [v] writeVal{1} [v] moveDir{R} }
$RULE_setrest0_{ [v] nextState{continue} [v] writeVal{0} [v] moveDir{L} }

$RULE_setrest1_0{ [v] nextState{setrest1} [v] writeVal{0} [v] moveDir{R} }
$RULE_setrest1_1{ [v] nextState{setrest1} [v] writeVal{1} [v] moveDir{R} }
$RULE_setrest1_{ [v] nextState{continue} [v] writeVal{1} [v] moveDir{L} }

[comment] { ESTADO continue: volver al inicio del numero decimal }
$RULE_continue_0{ [v] nextState{continue} [v] writeVal{0} [v] moveDir{L} }
$RULE_continue_1{ [v] nextState{continue} [v] writeVal{1} [v] moveDir{L} }
$RULE_continue_{ [v] nextState{continue2} [v] writeVal{_} [v] moveDir{L} }

[comment] { ESTADO continue2: Delimitador, volver a dividir }
$RULE_continue2_{ [v] nextState{halve} [v] writeVal{0} [v] moveDir{L} }

[comment] { ESTADO qfin: Aceptacion }

[comment] {------------------- HELPERS DE CONTROL -------------------}

[comment] { While-loop dispatch }
$action_true{$tml.step}
$action_false{}

[comment] { Halt detection }
$CHECK_HALT_qfin{ [v] tm:running{false} }

[comment] {------------------- CLASE TuringMachine (con cinta infinita v2) -------------------}
#class TuringMachine {
    #begin-construction

    #method:init {
        [comment] {--- Tape ---}
        [variable] tb:head{I}
        [variable] _cellPrefix_{I()}
        [variable] _rightLinkPrefix_{right_T()}
        [variable] _leftLinkPrefix_{left_T()}
        [variable] _modeRightPrefix_{mR()}
        [variable] _modeLeftPrefix_{mL()}
        [variable] _doRightPrefix_{#this._doRight_}
        [variable] _doLeftPrefix_{#this._doLeft_}
        [execute] _cellPrefix_ I) {_}
        [execute] _cellPrefix_ -I) {_}
        [execute] _rightLinkPrefix_ -I) {I}
        [execute] _leftLinkPrefix_ I) {-I}
        [execute] _rightLinkPrefix_ -I ) {I}
        [execute] _leftLinkPrefix_ I ) {-I}
        [execute] _modeRightPrefix_ I ) {c}
        [execute] _modeRightPrefix_ -I ) {m}
        [execute] _modeLeftPrefix_ I ) {m}
        [execute] _modeLeftPrefix_ -I ) {c}
        [comment] {--- TM ---}
        [variable] tm:currentState{qinit}
        [variable] tm:running{true}
        [variable] _moveDispatch_{#this._move_}
        [variable] _skipDispatch_{#this._skip_}
        [variable] _readBitsPrefix_{#this._readBit}
    }
    [comment] {--- Tape: mover derecha/izquierda con crear-o-navegar ---}
    #method:_move_R {
        [run]{ [variable] _mode_{#this.modeR} }
        [run]{ [variable] _method_{[execute]_doRightPrefix_ $_mode_} }
        [run]{ [execute] _method_ }
    }
    #method:_doRight_c {
        [run]{ [variable] _next_{$tb:headI} }
        [execute] _cellPrefix_ $_next_ ) {_}
        [execute] _rightLinkPrefix_ $tb:head ) {$_next_}
        [execute] _leftLinkPrefix_ $_next_ ) {$tb:head}
        [execute] _modeRightPrefix_ $tb:head ) {m}
        [execute] _modeRightPrefix_ $_next_ ) {c}
        [execute] _modeLeftPrefix_ $_next_ ) {m}
        [variable] tb:head{$_next_}
    }
    #method:_doRight_m {
        [variable] tb:head{#this.rightNeighbor}
    }
    #method:_move_L {
        [run]{ [variable] _mode_{#this.model} }
        [run]{ [variable] _method_{[execute]_doLeftPrefix_ $_mode_} }
        [run]{ [execute] _method_ }
    }
    #method:_doLeft_c {
        [execute] _cellPrefix_ $_next_ ) {_}
        [execute] _leftLinkPrefix_ $tb:head ) {$_next_}
        [execute] _rightLinkPrefix_ $_next_ ) {$tb:head}
        [execute] _modeLeftPrefix_ $tb:head ) {m}
        [execute] _modeLeftPrefix_ $_next_ ) {c}
        [execute] _modeRightPrefix_ $_next_ ) {m}
        [variable] tb:head{$_next_}
    }
    #method:_doLeft_m {
        [variable] tb:head{#this.leftNeighbor}
    }
    #method:_move_N { }

    [comment] {--- Escribir en celda actual ---}
    #method:_write_0 { [execute] _cellPrefix_ $tb:head ) {0} }
    #method:_write_1 { [execute] _cellPrefix_ $tb:head ) {1} }
    #method:_write_2 { [execute] _cellPrefix_ $tb:head ) {2} }
    #method:_write_3 { [execute] _cellPrefix_ $tb:head ) {3} }
    #method:_write_4 { [execute] _cellPrefix_ $tb:head ) {4} }
    #method:_write_5 { [execute] _cellPrefix_ $tb:head ) {5} }
    #method:_write_6 { [execute] _cellPrefix_ $tb:head ) {6} }
    #method:_write_7 { [execute] _cellPrefix_ $tb:head ) {7} }
    #method:_write_8 { [execute] _cellPrefix_ $tb:head ) {8} }
    #method:_write_9 { [execute] _cellPrefix_ $tb:head ) {9} }
    #method:_write_{ [execute] _cellPrefix_ $tb:head ) {_} }

    [comment] {--- While-loop ---}
    #method:ran {
        [run]{ [variable] isRunning{#this.running} }
        [run]{ [variable] b1{[execute]action_} }
        [run]{ [variable] nextAction{[execute]b1 $isRunning} }
        [run]{ [execute] nextAction }
    }

    [comment] {--- Step: un paso de la TM ---}
    #method:step {
        [comment] { 1. LEER celda actual }
        [run]{ [variable] currentSym{#this.cellVal} }

        [comment] { 2. BUSCAR regla por (estado, simbolo) }
        [run]{ [variable] state{#this.currentState_} }
        [run]{ [variable] _rule_{[execute]RULE_} }
        [run]{ [variable] ruleName{[execute]_rule_ $state $currentSym} }

        [comment] { 3. EJECUTAR regla (setea nextState, writeVal, moveDir) }
        [run]{ [execute] ruleName }

        [comment] { 4. ESCRIBIR en celda actual }
        [run]{ [execute] _cellPrefix_ $tb:head ) {$writeVal} }

        [comment] { 5. MOVER cabezal }
        [run]{ [variable] _moveMethod_{[execute]_moveDispatch_ $moveDir} }
        [run]{ [execute] _moveMethod_ }

        [comment] { 6. ACTUALIZAR estado }
        [run]{ #property-> currentState{$nextState} }

        [comment] { 7. VERIFICAR halt }
        [run]{ [variable] _haltCheck_{[execute]CHECK_HALT_} }
        [run]{ [variable] _haltAction_{[execute]_haltCheck_ $nextState} }
        [run]{ [execute] _haltAction_ }

        [comment] { 8. CONTINUAR (recursion) }
        [run]{ [execute] tml.ran }
    }

    [comment] {--- Mostrar resultado: saltar blanks y leer bits ---}
    #method:showResult {
        [run]{ [variable] tb:head{I} }
        [run]{ [variable] _symbol_{#this.cellVal} }
        [run]{ [variable] _skipMethod_{[execute]_skipDispatch_ $_symbol_} }
        [run]{ [execute] _skipMethod_ }
    }
    #method:_skip_ {
        [run]{ [variable] tb:head{#this.rightNeighbor} }
        [run]{ [variable] _symbol_{#this.cellVal} }
        [run]{ [variable] _skipMethod_{[execute]_skipDispatch_ $_symbol_} }
        [run]{ [execute] _skipMethod_ }
    }
    [comment] { Al encontrar 0 o 1: leer los bits del resultado }
    #method:_skip_0 {
        [run]{ [variable] _readMethod_{[execute]_readBitsPrefix_ s} }
        [run]{ [execute] _readMethod_ }
    }
    #method:_skip_1 {
        [run]{ [variable] _readMethod_{[execute]_readBitsPrefix_ s} }
        [run]{ [execute] _readMethod_ }
    }
    #method:_readBits {
        [run]{ [variable] _cellName_{#this.cellVal} }
        [run]{ [variable] RESULT_BIT0{[execute]_cellName_} }
        [run]{ [variable] tb:head{#this.rightNeighbor} }
        [run]{ [variable] _cellName_{#this.cellVal} }
        [run]{ [variable] RESULT_BIT1{[execute]_cellName_} }
        [run]{ [variable] tb:head{#this.rightNeighbor} }
        [run]{ [variable] _cellName_{#this.cellVal} }
        [run]{ [variable] RESULT_BIT2{[execute]_cellName_} }
        [run]{ [variable] tb:head{#this.rightNeighbor} }
        [run]{ [variable] _cellName_{#this.cellVal} }
        [run]{ [variable] RESULT_BIT3{[execute]_cellName_} }
        [run]{ [variable] tb:head{#this.rightNeighbor} }
        [run]{ [variable] _cellName_{#this.cellVal} }
        [run]{ [variable] RESULT_BIT4{[execute]_cellName_} }
        [run]{ [variable] tb:head{#this.rightNeighbor} }
        [run]{ [variable] _cellName_{#this.cellVal} }
        [run]{ [variable] RESULT_BIT5{[execute]_cellName_} }
        [run]{ [variable] tb:head{#this.rightNeighbor} }
        [run]{ [variable] _cellName_{#this.cellVal} }
        [run]{ [variable] RESULT_BIT6{[execute]_cellName_} }
        [run]{ [variable] tb:head{#this.rightNeighbor} }
        [run]{ [variable] _cellName_{#this.cellVal} }
        [run]{ [variable] RESULT_BIT7{[execute]_cellName_} }
    }

    [comment] {--- Properties ---}
    #property:cellVal { [execute] _cellPrefix_ $tb:head ) }
    #property:rightNeighbor { [execute] _rightLinkPrefix_ $tb:head ) }
    #property:leftNeighbor { [execute] _leftLinkPrefix_ $tb:head ) }
    #property:modeR { [execute] _modeRightPrefix_ $tb:head ) }
    #property:model { [execute] _modeLeftPrefix_ $tb:head ) }
    #property:currentState { [execute] tm:currentState }
    #property:running { [execute] tm:running }

    #end-construction
}

[comment] {------------------- MAIN ---------------------}
[run] {
    [run] { $instance:name{tm1} }
    [run] { new TuringMachine }
    [run] { $tm1.init }

    [comment] { Escribir input "11" (decimal once): T(I)=1, T(II)=1 }
    [run] { $tm1._write_1 }
    [run] { $tm1._move_R }
    [run] { $tm1._write_1 }
    [run] { $tm1._move_L }

    [comment] { Ejecutar TM }
    [run] { $tm1.ran }

    [comment] { Mostrar resultado }
    [run] { $tm1.showResult }
}
```
