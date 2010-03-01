Выкинуть с повторяющимися буквами!


cat muller.txt | grep -e "   [a-z][a-z][a-z][a-z][a-z] .*noun" |  sed -e 's/   //' | cut -f1 -d' ' | uniq> nouns_5.txt
