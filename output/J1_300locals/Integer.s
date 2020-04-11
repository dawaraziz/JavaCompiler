extern java.lang.Object_getClass
extern java.lang.Object_clone
extern java.lang.Object_hashCode
extern java.lang.Object_toString
extern java.lang.Object_equals_java.lang.Object
extern default#.J1_300locals_test
extern default#.J1_300locals_J1_300locals
extern java.io.OutputStream_flush
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
extern __malloc
extern __debexit
extern __exception
extern NATIVEjava.io.OutputStream.nativeWrite
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
global java.lang.Integer_toString
global java.lang.Integer_parseInt_java.lang.String
global java.lang.Integer_intValue
global java.lang.Integer_Integer
global java.lang.Integer_Integer_java.lang.String
global java.lang.Integer_Integer_INT
section .data
java.lang.Integer_vtable:
dd java.lang.Integer_sitrow ; Pointer to the SIT.
dd java.lang.Integer_subtypeTable ; Pointer to the subtype table.
dd java.lang.Object_getClass
dd java.lang.Object_clone
dd java.lang.Object_hashCode
dd java.lang.Integer_toString
dd java.lang.Object_equals_java.lang.Object
dd java.lang.Integer_intValue
dd java.lang.Integer_parseInt_java.lang.String
section .text ; Code for the method java.lang.Integer_toString
java.lang.Integer_toString:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi



push eax
push 0 ; Static method has null this pointer.
call java.lang.String_valueOf_INT
add esp, 8
jmp java.lang.Integer_toString@end_method

java.lang.Integer_toString@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.Integer_parseInt_java.lang.String
java.lang.Integer_parseInt_java.lang.String:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi


mov eax, 0
mov [ebp + -12], eax

mov eax, 0
mov [ebp + -16], eax

mov eax, 0
mov [ebp + -20], eax
while_loop_1:
cmp eax, 0; Check if expression returns false.
je while_end_1; Jump to end of loop if expr. is false.
cmp eax, 0 ; Check if expression returns false.
je if_else_1; Jump to else if expr. is false.

mov eax, ebp ; Get stack base pointer.
add eax, -16 ; Add local variable offset.

push eax

mov eax, [ebp + -16]; Get local variable value.

cmp eax, 0
je unary_not_plus_not_0
mov eax, 0; If eax is 1+, set it to 0.
jmp unary_not_plus_end_0
unary_not_plus_not_0:
mov eax, 1; If eax is 0, set it to 1.
unary_not_plus_end_0:
pop ebx
mov [ebx], eax
jmp if_end_1
if_else_1:

mov eax, ebp ; Get stack base pointer.
add eax, -12 ; Add local variable offset.

push eax

push eax ; Push add. expr. LSH.
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
if_end_1:

mov eax, ebp ; Get stack base pointer.
add eax, -20 ; Add local variable offset.

push eax


mov eax, [ebp + -20]; Get local variable value.

push eax ; Push add. expr. LSH.
mov eax, 1
pop ebx ; Pop add. expr. LSH.
add eax, ebx

pop ebx
mov [ebx], eax
jmp while_loop_1; Jump to top of loop.
while_end_1:

mov eax, [ebp + -16]; Get local variable value.

cmp eax, 0 ; Check if expression returns false.
je if_else_2; Jump to else if expr. is false.

mov eax, ebp ; Get stack base pointer.
add eax, -12 ; Add local variable offset.

push eax

mov eax, [ebp + -12]; Get local variable value.

neg eax
pop ebx
mov [ebx], eax
jmp if_end_2
if_else_2:
if_end_2:

mov eax, [ebp + -12]; Get local variable value.

jmp java.lang.Integer_parseInt_java.lang.String@end_method

java.lang.Integer_parseInt_java.lang.String@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the method java.lang.Integer_intValue
java.lang.Integer_intValue:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi



jmp java.lang.Integer_intValue@end_method

java.lang.Integer_intValue@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the constructor java.lang.Integer_Integer
java.lang.Integer_Integer:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi
push dword [ebp + 8]
call java.lang.Number_Number
add esp, 4



push eax
mov eax, 0
pop ebx
mov [ebx], eax

java.lang.Integer_Integer@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the constructor java.lang.Integer_Integer_java.lang.String
java.lang.Integer_Integer_java.lang.String:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi
push dword [ebp + 8]
call java.lang.Number_Number
add esp, 4



push eax


push eax
push 0 ; Static method has null this pointer.
call java.lang.Integer_parseInt_java.lang.String
add esp, 8
pop ebx
mov [ebx], eax

java.lang.Integer_Integer_java.lang.String@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

section .text ; Code for the constructor java.lang.Integer_Integer_INT
java.lang.Integer_Integer_INT:
push ebp ; Saves the ebp.
mov ebp, esp ; Saves the esp.
push ebx
push esi
push edi
push dword [ebp + 8]
call java.lang.Number_Number
add esp, 4



push eax


pop ebx
mov [ebx], eax

java.lang.Integer_Integer_INT@end_method:
mov edi, [ebp - 12]
mov esi, [ebp - 8]
mov ebx, [ebp - 4]
mov esp, ebp ; Restores the esp.
pop ebp ; Restores the ebp.
ret

