import sys

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'text = "Test  of : ",',
    'text = "Test ${pagerState.currentPage + 1} of ${configs.size}: ${configs[pagerState.currentPage].testName.replace(\'_\', \' \')}",'
)

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.write(content)
