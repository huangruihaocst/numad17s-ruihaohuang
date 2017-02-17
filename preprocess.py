f = open('dictionary_word_list.txt', 'r')
words_str = f.read()
words = words_str.split('\n')
wf = open('nine_letters_word_list.txt', 'w')
i = 0
for word in words:
	if len(word) == 9:
		wf.write(word + '\n')
		i += 1
print(i)
