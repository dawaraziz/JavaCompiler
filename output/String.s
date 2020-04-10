extern java.lang.Object_getClass
extern java.lang.Object_clone
extern java.lang.Object_hashCode
extern java.lang.Object_toString
extern java.lang.Object_equals_java.lang.Object
extern default#.J1_300locals_test
extern default#.J1_300locals_J1_300locals
extern java.io.OutputStream_flush
extern java.io.OutputStream_nativeWrite_INT
extern java.io.OutputStream_write_INT
extern java.io.OutputStream_write_CHAR
extern java.io.OutputStream_OutputStream
extern java.io.PrintStream_print_INT
extern java.io.PrintStream_print_SHORT
extern java.io.PrintStream_print_CHAR
extern java.io.PrintStream_print_BYTE
extern java.io.PrintStream_print_BOOLEAN
extern java.io.PrintStream_print_java.lang.Object
extern java.io.PrintStream_println_INT
extern java.io.PrintStream_println_SHORT
extern java.io.PrintStream_println_CHAR
extern java.io.PrintStream_println_BYTE
extern java.io.PrintStream_println_BOOLEAN
extern java.io.PrintStream_println_java.lang.Object
extern java.io.PrintStream_println_java.lang.String
extern java.io.PrintStream_println
extern java.io.PrintStream_print_java.lang.String
extern java.io.PrintStream_PrintStream
extern java.lang.Boolean_toString
extern java.lang.Boolean_Boolean
extern java.lang.Boolean_Boolean_BOOLEAN
extern java.lang.Byte_toString
extern java.lang.Byte_Byte
extern java.lang.Byte_Byte_BYTE
extern java.lang.Character_toString
extern java.lang.Character_Character
extern java.lang.Character_Character_CHAR
extern java.lang.Class_Class
extern java.lang.Integer_toString
extern java.lang.Integer_intValue
extern java.lang.Integer_parseInt_java.lang.String
extern java.lang.Integer_Integer
extern java.lang.Integer_Integer_java.lang.String
extern java.lang.Integer_Integer_INT
extern java.lang.Number_intValue
extern java.lang.Number_Number
extern java.lang.Object_Object
extern java.lang.Short_toString
extern java.lang.Short_intValue
extern java.lang.Short_Short
extern java.lang.Short_Short_SHORT
extern java.lang.System_gc
extern java.lang.System_System
extern java.util.Arrays_equals_CHAR_CHAR
extern java.util.Arrays_equals_BOOLEAN_BOOLEAN
extern java.util.Arrays_Arrays
extern default#.J1_300locals_sitrow
extern java.io.OutputStream_sitrow
extern java.io.PrintStream_sitrow
extern java.io.Serializable_sitrow
extern java.lang.Boolean_sitrow
extern java.lang.Byte_sitrow
extern java.lang.Character_sitrow
extern java.lang.Class_sitrow
extern java.lang.Cloneable_sitrow
extern java.lang.Integer_sitrow
extern java.lang.Number_sitrow
extern java.lang.Object_sitrow
extern java.lang.Short_sitrow
extern java.lang.String_sitrow
extern java.lang.System_sitrow
extern java.util.Arrays_sitrow
extern default#.J1_300locals_subtypeTable
extern java.io.OutputStream_subtypeTable
extern java.io.PrintStream_subtypeTable
extern java.io.Serializable_subtypeTable
extern java.lang.Boolean_subtypeTable
extern java.lang.Byte_subtypeTable
extern java.lang.Character_subtypeTable
extern java.lang.Class_subtypeTable
extern java.lang.Cloneable_subtypeTable
extern java.lang.Integer_subtypeTable
extern java.lang.Number_subtypeTable
extern java.lang.Object_subtypeTable
extern java.lang.Short_subtypeTable
extern java.lang.String_subtypeTable
extern java.lang.System_subtypeTable
extern java.util.Arrays_subtypeTable
extern java.lang.Boolean_static_MAX_VALUE
extern java.lang.Byte_static_MAX_VALUE
extern java.lang.Integer_static_MAX_VALUE
extern java.lang.System_static_out
global java.lang.String_indexOf_java.lang.String
global java.lang.String_toCharArray
global java.lang.String_compareTo_java.lang.String
global java.lang.String_compareTo_java.lang.Object
global java.lang.String_toString
global java.lang.String_hashCode
global java.lang.String_trim
global java.lang.String_substring_INT_INT
global java.lang.String_equals_java.lang.Object
global java.lang.String_valueOf_java.lang.String
global java.lang.String_valueOf_java.lang.Object
global java.lang.String_valueOf_BOOLEAN
global java.lang.String_valueOf_BYTE
global java.lang.String_valueOf_SHORT
global java.lang.String_valueOf_INT
global java.lang.String_valueOf_CHAR
global java.lang.String_concat_java.lang.String
global java.lang.String_charAt_INT
global java.lang.String_length
global java.lang.String_String_java.lang.String
global java.lang.String_String_INT_CHAR
global java.lang.String_String_CHAR
global java.lang.String_String
section .data
java.lang.String_vtable:
dd java.lang.String_sitrow ; Pointer to the SIT.
dd java.lang.String_subtypeTable ; Pointer to the subtype table.
dd java.lang.Object_getClass
dd java.lang.Object_clone
dd java.lang.String_hashCode
dd java.lang.String_toString
dd java.lang.Object_equals_java.lang.Object
dd java.lang.String_indexOf_java.lang.String
dd java.lang.String_toCharArray
dd java.lang.String_compareTo_java.lang.String
dd java.lang.String_compareTo_java.lang.Object
dd java.lang.String_trim
dd java.lang.String_substring_INT_INT
dd java.lang.String_equals_java.lang.Object
dd java.lang.String_valueOf_java.lang.String
dd java.lang.String_valueOf_java.lang.Object
dd java.lang.String_valueOf_BOOLEAN
dd java.lang.String_valueOf_BYTE
dd java.lang.String_valueOf_SHORT
dd java.lang.String_valueOf_INT
dd java.lang.String_valueOf_CHAR
dd java.lang.String_concat_java.lang.String
dd java.lang.String_charAt_INT
dd java.lang.String_length
section .text ; Code for the method java.lang.String_indexOf_java.lang.String
java.lang.String_indexOf_java.lang.String:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi


mov eax, 0
push [ebp + -12], eax

mov eax, 0
push [ebp + -16], eax
for_loop_2:

mov eax, ebp + -12; Get local variable value.

push eax
mov eax, 0
pop ebx
mov [ebx], eax
cmp eax, 0; Check if expression returns false.
je for_end_2; Jump to end of loop if expr. is false.

mov eax, 1
push [ebp + -20], eax
for_loop_3:

mov eax, ebp + -16; Get local variable value.

push eax
mov eax, 0
pop ebx
mov [ebx], eax
cmp eax, 0; Check if expression returns false.
je for_end_3; Jump to end of loop if expr. is false.
cmp eax, 0 ; Check if expression returns false.
je if_else_3; Jump to else if expr. is false.

mov eax, ebp + -20; Get local variable value.

push eax
mov eax, 0
pop ebx
mov [ebx], eax
jmp if_end_3
if_else_3:
cmp eax, 0 ; Check if expression returns false.
je if_else_4; Jump to else if expr. is false.

mov eax, ebp + -20; Get local variable value.

push eax
mov eax, 0
pop ebx
mov [ebx], eax
jmp if_end_4
if_else_4:
if_end_4:
if_end_4:

mov eax, ebp + -16; Get local variable value.

push eax


mov eax, [ebp + -16]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp for_loop_3; Jump to top of loop.
for_end_3:

mov eax, [ebp + -20]; Get local variable value.

cmp eax, 0 ; Check if expression returns false.
je if_else_5; Jump to else if expr. is false.

mov eax, [ebp + -12]; Get local variable value.

jmp java.lang.String_indexOf_java.lang.String@end_method
jmp if_end_5
if_else_5:
if_end_5:

mov eax, ebp + -12; Get local variable value.

push eax


mov eax, [ebp + -12]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp for_loop_3; Jump to top of loop.
for_end_3:
mov eax, 1
neg eax
jmp java.lang.String_indexOf_java.lang.String@end_method

java.lang.String_indexOf_java.lang.String@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_toCharArray
java.lang.String_toCharArray:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi


push [ebp + -12], eax
for_loop_4:

mov eax, 0
push [ebp + -16], eax
cmp eax, 0; Check if expression returns false.
je for_end_4; Jump to end of loop if expr. is false.
push eax
pop ebx
mov [ebx], eax

mov eax, ebp + -16; Get local variable value.

push eax


mov eax, [ebp + -16]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp for_loop_4; Jump to top of loop.
for_end_4:

mov eax, [ebp + -12]; Get local variable value.

jmp java.lang.String_toCharArray@end_method

java.lang.String_toCharArray@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_compareTo_java.lang.String
java.lang.String_compareTo_java.lang.String:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi


mov eax, 0
push [ebp + -12], eax

mov eax, 1
push [ebp + -16], eax
while_loop_0:

mov eax, [ebp + -16]; Get local variable value.

cmp eax, 0; Check if expression returns false.
je while_end_0; Jump to end of loop if expr. is false.
cmp eax, 0 ; Check if expression returns false.
je if_else_6; Jump to else if expr. is false.
mov eax, 0
jmp java.lang.String_compareTo_java.lang.String@end_method
jmp if_end_6
if_else_6:
if_end_6:
cmp eax, 0 ; Check if expression returns false.
je if_else_7; Jump to else if expr. is false.
mov eax, 1
neg eax
jmp java.lang.String_compareTo_java.lang.String@end_method
jmp if_end_7
if_else_7:
if_end_7:
cmp eax, 0 ; Check if expression returns false.
je if_else_8; Jump to else if expr. is false.
mov eax, 1
jmp java.lang.String_compareTo_java.lang.String@end_method
jmp if_end_8
if_else_8:
if_end_8:
cmp eax, 0 ; Check if expression returns false.
je if_else_9; Jump to else if expr. is false.
mov eax, 1
neg eax
jmp java.lang.String_compareTo_java.lang.String@end_method
jmp if_end_9
if_else_9:
if_end_9:
cmp eax, 0 ; Check if expression returns false.
je if_else_10; Jump to else if expr. is false.
mov eax, 1
jmp java.lang.String_compareTo_java.lang.String@end_method
jmp if_end_10
if_else_10:
if_end_10:

mov eax, ebp + -12; Get local variable value.

push eax


mov eax, [ebp + -12]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp while_loop_0; Jump to top of loop.
while_end_0:
mov eax, 0
jmp java.lang.String_compareTo_java.lang.String@end_method

java.lang.String_compareTo_java.lang.String@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_compareTo_java.lang.Object
java.lang.String_compareTo_java.lang.Object:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi



push eax
mov eax, [ebp + 8]
push eax
call java.lang.String_compareTo_java.lang.String
add esp, 8
jmp java.lang.String_compareTo_java.lang.Object@end_method

java.lang.String_compareTo_java.lang.Object@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_toString
java.lang.String_toString:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi

mov eax, [ebp + 8] ; Get this pointer
jmp java.lang.String_toString@end_method

java.lang.String_toString@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_hashCode
java.lang.String_hashCode:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi


mov eax, 0
push [ebp + -12], eax
for_loop_5:

mov eax, 0
push [ebp + -16], eax
cmp eax, 0; Check if expression returns false.
je for_end_5; Jump to end of loop if expr. is false.

mov eax, ebp + -12; Get local variable value.

push eax

push eax ; Push add. expr. LSH.
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax

mov eax, ebp + -16; Get local variable value.

push eax


mov eax, [ebp + -16]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp for_loop_5; Jump to top of loop.
for_end_5:

mov eax, [ebp + -12]; Get local variable value.

jmp java.lang.String_hashCode@end_method

java.lang.String_hashCode@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_trim
java.lang.String_trim:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi


mov eax, 0
push [ebp + -12], eax

mov eax, 0
push [ebp + -16], eax
for_loop_6:

mov eax, ebp + -12; Get local variable value.

push eax
mov eax, 0
pop ebx
mov [ebx], eax
cmp eax, 0; Check if expression returns false.
je for_end_6; Jump to end of loop if expr. is false.

mov eax, ebp + -12; Get local variable value.

push eax


mov eax, [ebp + -12]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp for_loop_6; Jump to top of loop.
for_end_6:
for_loop_7:

mov eax, ebp + -16; Get local variable value.

push eax

mov eax, [ebp + 8]
push eax
call java.lang.String_length
add esp, 4
push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
cmp eax, 0; Check if expression returns false.
je for_end_7; Jump to end of loop if expr. is false.

mov eax, ebp + -16; Get local variable value.

push eax


mov eax, [ebp + -16]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp for_loop_7; Jump to top of loop.
for_end_7:
cmp eax, 0 ; Check if expression returns false.
je if_else_11; Jump to else if expr. is false.
jmp java.lang.String_trim@end_method
jmp if_end_11
if_else_11:

mov eax, [ebp + -12]; Get local variable value.

push eax


mov eax, [ebp + -16]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

push eax
mov eax, [ebp + 8]
push eax
call java.lang.String_substring_INT_INT
add esp, 12
jmp java.lang.String_trim@end_method
if_end_11:

java.lang.String_trim@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_substring_INT_INT
java.lang.String_substring_INT_INT:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi


mov eax, 0
push [ebp + -12], eax
cmp eax, 0 ; Check if expression returns false.
je if_else_12; Jump to else if expr. is false.
jmp java.lang.String_substring_INT_INT@end_method
jmp if_end_12
if_else_12:
if_end_12:
cmp eax, 0 ; Check if expression returns false.
je if_else_13; Jump to else if expr. is false.
jmp java.lang.String_substring_INT_INT@end_method
jmp if_end_13
if_else_13:
if_end_13:
cmp eax, 0 ; Check if expression returns false.
je if_else_14; Jump to else if expr. is false.
jmp java.lang.String_substring_INT_INT@end_method
jmp if_end_14
if_else_14:
if_end_14:

push [ebp + -16], eax
for_loop_8:

mov eax, ebp + -12; Get local variable value.

push eax


pop ebx
mov [ebx], eax
cmp eax, 0; Check if expression returns false.
je for_end_8; Jump to end of loop if expr. is false.
push eax

mov eax, [ebp + -12]; Get local variable value.

push eax
mov eax, [ebp + 8]
push eax
call java.lang.String_charAt_INT
add esp, 8
pop ebx
mov [ebx], eax

mov eax, ebp + -12; Get local variable value.

push eax


mov eax, [ebp + -12]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp for_loop_8; Jump to top of loop.
for_end_8:

mov eax, 0
push [ebp + -20], eax
jmp java.lang.String_substring_INT_INT@end_method

java.lang.String_substring_INT_INT@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_equals_java.lang.Object
java.lang.String_equals_java.lang.Object:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi

cmp eax, 0 ; Check if expression returns false.
je if_else_15; Jump to else if expr. is false.
mov eax, 0
jmp java.lang.String_equals_java.lang.Object@end_method
jmp if_end_15
if_else_15:
if_end_15:
cmp eax, 0
je unary_not_plus_not_0
mov eax, 0; If eax is 1+, set it to 0.
jmp unary_not_plus_end_0
unary_not_plus_not_0:
mov eax, 1; If eax is 0, set it to 1.
unary_not_plus_end_0:
cmp eax, 0 ; Check if expression returns false.
je if_else_16; Jump to else if expr. is false.
mov eax, 0
jmp java.lang.String_equals_java.lang.Object@end_method
jmp if_end_16
if_else_16:
if_end_16:


push eax
push eax
push 0 ; Static method has null this pointer.
call java.util.Arrays_equals_CHAR_CHAR
add esp, 12
jmp java.lang.String_equals_java.lang.Object@end_method

java.lang.String_equals_java.lang.Object@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_valueOf_java.lang.String
java.lang.String_valueOf_java.lang.String:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi

cmp eax, 0 ; Check if expression returns false.
je if_else_17; Jump to else if expr. is false.
jmp java.lang.String_valueOf_java.lang.String@end_method
jmp if_end_17
if_else_17:


jmp java.lang.String_valueOf_java.lang.String@end_method
if_end_17:

java.lang.String_valueOf_java.lang.String@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_valueOf_java.lang.Object
java.lang.String_valueOf_java.lang.Object:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi

cmp eax, 0 ; Check if expression returns false.
je if_else_18; Jump to else if expr. is false.
jmp java.lang.String_valueOf_java.lang.Object@end_method
jmp if_end_18
if_else_18:
mov eax, [ebp + 8]
push eax
call java.lang.Object_toString
add esp, 4
jmp java.lang.String_valueOf_java.lang.Object@end_method
if_end_18:

java.lang.String_valueOf_java.lang.Object@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_valueOf_BOOLEAN
java.lang.String_valueOf_BOOLEAN:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi



cmp eax, 0 ; Check if expression returns false.
je if_else_19; Jump to else if expr. is false.
jmp java.lang.String_valueOf_BOOLEAN@end_method
jmp if_end_19
if_else_19:
jmp java.lang.String_valueOf_BOOLEAN@end_method
if_end_19:

java.lang.String_valueOf_BOOLEAN@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_valueOf_BYTE
java.lang.String_valueOf_BYTE:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi



push eax
push 0 ; Static method has null this pointer.
call java.lang.String_valueOf_INT
add esp, 8
jmp java.lang.String_valueOf_BYTE@end_method

java.lang.String_valueOf_BYTE@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_valueOf_SHORT
java.lang.String_valueOf_SHORT:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi



push eax
push 0 ; Static method has null this pointer.
call java.lang.String_valueOf_INT
add esp, 8
jmp java.lang.String_valueOf_SHORT@end_method

java.lang.String_valueOf_SHORT@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_valueOf_INT
java.lang.String_valueOf_INT:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi


push [ebp + -12], eax

mov eax, 0
push [ebp + -16], eax

mov eax, 0
push [ebp + -20], eax
cmp eax, 0 ; Check if expression returns false.
je if_else_20; Jump to else if expr. is false.
jmp java.lang.String_valueOf_INT@end_method
jmp if_end_20
if_else_20:
if_end_20:
cmp eax, 0 ; Check if expression returns false.
je if_else_21; Jump to else if expr. is false.

mov eax, ebp + -20; Get local variable value.

push eax
mov eax, 1
pop ebx
mov [ebx], eax


push eax


neg eax
pop ebx
mov [ebx], eax
jmp if_end_21
if_else_21:
if_end_21:
cmp eax, 0 ; Check if expression returns false.
je if_else_22; Jump to else if expr. is false.
push eax
mov eax, 48
pop ebx
mov [ebx], eax

mov eax, ebp + -16; Get local variable value.

push eax


mov eax, [ebp + -16]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp if_end_22
if_else_22:
while_loop_0:
cmp eax, 0; Check if expression returns false.
je while_end_0; Jump to end of loop if expr. is false.

push [ebp + -24], eax


push eax
pop ebx
mov [ebx], eax
push eax


mov eax, [ebp + -24]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 48
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax

mov eax, ebp + -16; Get local variable value.

push eax


mov eax, [ebp + -16]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp while_loop_0; Jump to top of loop.
while_end_0:
if_end_22:

mov eax, [ebp + -20]; Get local variable value.

cmp eax, 0 ; Check if expression returns false.
je if_else_23; Jump to else if expr. is false.
push eax
mov eax, 45
pop ebx
mov [ebx], eax

mov eax, ebp + -16; Get local variable value.

push eax


mov eax, [ebp + -16]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp if_end_23
if_else_23:
if_end_23:

push [ebp + -28], eax
for_loop_9:


push eax
mov eax, 0
pop ebx
mov [ebx], eax
cmp eax, 0; Check if expression returns false.
je for_end_9; Jump to end of loop if expr. is false.
push eax
pop ebx
mov [ebx], eax


push eax



push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp for_loop_9; Jump to top of loop.
for_end_9:
jmp java.lang.String_valueOf_INT@end_method

java.lang.String_valueOf_INT@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_valueOf_CHAR
java.lang.String_valueOf_CHAR:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi


push [ebp + -12], eax
push eax


pop ebx
mov [ebx], eax
jmp java.lang.String_valueOf_CHAR@end_method

java.lang.String_valueOf_CHAR@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_concat_java.lang.String
java.lang.String_concat_java.lang.String:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi


mov eax, 0
push [ebp + -12], eax

push [ebp + -16], eax
for_loop_10:

mov eax, ebp + -12; Get local variable value.

push eax
mov eax, 0
pop ebx
mov [ebx], eax
cmp eax, 0; Check if expression returns false.
je for_end_10; Jump to end of loop if expr. is false.
push eax
pop ebx
mov [ebx], eax

mov eax, ebp + -12; Get local variable value.

push eax


mov eax, [ebp + -12]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp for_loop_10; Jump to top of loop.
for_end_10:
for_loop_11:

mov eax, ebp + -12; Get local variable value.

push eax
mov eax, 0
pop ebx
mov [ebx], eax
cmp eax, 0; Check if expression returns false.
je for_end_11; Jump to end of loop if expr. is false.
push eax
pop ebx
mov [ebx], eax

mov eax, ebp + -12; Get local variable value.

push eax


mov eax, [ebp + -12]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp for_loop_11; Jump to top of loop.
for_end_11:
jmp java.lang.String_concat_java.lang.String@end_method

java.lang.String_concat_java.lang.String@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_charAt_INT
java.lang.String_charAt_INT:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi

jmp java.lang.String_charAt_INT@end_method

java.lang.String_charAt_INT@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.String_length
java.lang.String_length:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi



jmp java.lang.String_length@end_method

java.lang.String_length@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the constructor java.lang.String_String_java.lang.String
java.lang.String_String_java.lang.String:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi
push [ebx + 12]
call java.lang.Object_Object
add esp, 4

push eax


pop ebx
mov [ebx], eax

java.lang.String_String_java.lang.String@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the constructor java.lang.String_String_INT_CHAR
java.lang.String_String_INT_CHAR:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi
push [ebx + 16]
call java.lang.Object_Object
add esp, 4

push eax
pop ebx
mov [ebx], eax
for_loop_12:

mov eax, 0
push [ebp + -12], eax
cmp eax, 0; Check if expression returns false.
je for_end_12; Jump to end of loop if expr. is false.
push eax
pop ebx
mov [ebx], eax

mov eax, ebp + -12; Get local variable value.

push eax


mov eax, [ebp + -12]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp for_loop_12; Jump to top of loop.
for_end_12:

java.lang.String_String_INT_CHAR@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the constructor java.lang.String_String_CHAR
java.lang.String_String_CHAR:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi
push [ebx + 12]
call java.lang.Object_Object
add esp, 4

push eax
pop ebx
mov [ebx], eax
for_loop_13:

mov eax, 0
push [ebp + -12], eax
cmp eax, 0; Check if expression returns false.
je for_end_13; Jump to end of loop if expr. is false.
push eax
pop ebx
mov [ebx], eax

mov eax, ebp + -12; Get local variable value.

push eax


mov eax, [ebp + -12]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp for_loop_13; Jump to top of loop.
for_end_13:

java.lang.String_String_CHAR@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the constructor java.lang.String_String
java.lang.String_String:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi
push [ebx + 8]
call java.lang.Object_Object
add esp, 4



push eax
pop ebx
mov [ebx], eax

java.lang.String_String@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

