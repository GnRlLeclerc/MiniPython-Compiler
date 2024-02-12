	.text
	.globl main
main:
	movq $cst, %rdi
	movq $0, %rsi
	call printf
	.data
cst:
	.string "help"
