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
extern java.lang.String_hashCode
extern java.lang.String_toString
extern java.lang.String_indexOf_java.lang.String
extern java.lang.String_toCharArray
extern java.lang.String_compareTo_java.lang.String
extern java.lang.String_compareTo_java.lang.Object
extern java.lang.String_trim
extern java.lang.String_substring_INT_INT
extern java.lang.String_equals_java.lang.Object
extern java.lang.String_valueOf_java.lang.String
extern java.lang.String_valueOf_java.lang.Object
extern java.lang.String_valueOf_BOOLEAN
extern java.lang.String_valueOf_BYTE
extern java.lang.String_valueOf_SHORT
extern java.lang.String_valueOf_INT
extern java.lang.String_valueOf_CHAR
extern java.lang.String_concat_java.lang.String
extern java.lang.String_charAt_INT
extern java.lang.String_length
extern java.lang.String_String_java.lang.String
extern java.lang.String_String_INT_CHAR
extern java.lang.String_String_CHAR
extern java.lang.String_String
extern java.lang.System_gc
extern java.lang.System_System
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
global java.util.Arrays_equals_CHAR_CHAR
global java.util.Arrays_equals_BOOLEAN_BOOLEAN
global java.util.Arrays_Arrays
section .data
java.util.Arrays_vtable:
dd java.util.Arrays_sitrow ; Pointer to the SIT.
dd java.util.Arrays_subtypeTable ; Pointer to the subtype table.
dd java.lang.Object_getClass
dd java.lang.Object_clone
dd java.lang.Object_hashCode
dd java.lang.Object_toString
dd java.lang.Object_equals_java.lang.Object
dd java.util.Arrays_equals_CHAR_CHAR
dd java.util.Arrays_equals_BOOLEAN_BOOLEAN
section .text ; Code for the method java.util.Arrays_equals_CHAR_CHAR
java.util.Arrays_equals_CHAR_CHAR:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi

cmp eax, 0 ; Check if expression returns false.
je if_else_24; Jump to else if expr. is false.
mov eax, 0
jmp java.util.Arrays_equals_CHAR_CHAR@end_method
jmp if_end_24
if_else_24:
if_end_24:
for_loop_14:

mov eax, 0
push [ebp + -12], eax
cmp eax, 0; Check if expression returns false.
je for_end_14; Jump to end of loop if expr. is false.
cmp eax, 0 ; Check if expression returns false.
je if_else_25; Jump to else if expr. is false.
mov eax, 0
jmp java.util.Arrays_equals_CHAR_CHAR@end_method
jmp if_end_25
if_else_25:
if_end_25:

mov eax, ebp + -12; Get local variable value.

push eax


mov eax, [ebp + -12]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp for_loop_14; Jump to top of loop.
for_end_14:
mov eax, 1
jmp java.util.Arrays_equals_CHAR_CHAR@end_method

java.util.Arrays_equals_CHAR_CHAR@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.util.Arrays_equals_BOOLEAN_BOOLEAN
java.util.Arrays_equals_BOOLEAN_BOOLEAN:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi

cmp eax, 0 ; Check if expression returns false.
je if_else_26; Jump to else if expr. is false.
mov eax, 0
jmp java.util.Arrays_equals_BOOLEAN_BOOLEAN@end_method
jmp if_end_26
if_else_26:
if_end_26:
for_loop_15:

mov eax, 0
push [ebp + -12], eax
cmp eax, 0; Check if expression returns false.
je for_end_15; Jump to end of loop if expr. is false.
cmp eax, 0 ; Check if expression returns false.
je if_else_27; Jump to else if expr. is false.
mov eax, 0
jmp java.util.Arrays_equals_BOOLEAN_BOOLEAN@end_method
jmp if_end_27
if_else_27:
if_end_27:

mov eax, ebp + -12; Get local variable value.

push eax


mov eax, [ebp + -12]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp for_loop_15; Jump to top of loop.
for_end_15:
mov eax, 1
jmp java.util.Arrays_equals_BOOLEAN_BOOLEAN@end_method

java.util.Arrays_equals_BOOLEAN_BOOLEAN@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the constructor java.util.Arrays_Arrays
java.util.Arrays_Arrays:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi
push [ebx + 8]
call java.lang.Object_Object
add esp, 4


java.util.Arrays_Arrays@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

