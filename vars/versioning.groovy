

def masterBranch() {
    def version = sh(script: "git tag -l '[0-9]*' | grep '^[0-9]*\$' | sort -rn | head -1", returnStdout: true).trim()
    version = (Integer.parseInt(version) + 1).toString()
    echo "Master branch, next version is: ${version}"
    return version
}


def featureBranch(branchName) {
    def version = ""
    final String DIGITS_ONLY = "^[a-zA-Z]*[-_](\\d*)[-._\\w]*\$"
    def jiraTicket = branchName.replaceAll(DIGITS_ONLY, "\$1")
    println "Jira issue number for branch is: '${jiraTicket}'"

    def lastBranchTag = sh(script: "git tag -l '*.${jiraTicket}.*' | sort -rn -t . -k 3 | head -1", returnStdout: true).trim()
    if (!lastBranchTag.isEmpty()) {
        println "Found previous branch build with tag: '${lastBranchTag}'"
        def prefix = lastBranchTag.split("\\.").init().join(".")
        def suffix = Integer.parseInt(lastBranchTag.split("\\.").last()) + 1
        version = "${prefix}.${suffix}"
    } else {
        def prefix = sh(script: "git describe --tags", returnStdout: true).trim()
        prefix = prefix.replaceAll("-.*", "") + "." + jiraTicket
        println "New branch. Branch builds will be prefixed with: '${prefix}'"
        version = "${prefix}.1"
    }
    echo "Feature branch, next version is: ${version}"
    return version
}

def fixBranch(branchName) {
    def version = ""
    def branchPrefix = branchName.replaceAll("-fix", "").replaceAll("-", "")
    def lastBranchTag = sh(script: "git tag -l '${branchPrefix}.*' | sort -rn -k 2 -t . | head -1", returnStdout: true).trim()
    if (lastBranchTag.isEmpty()) {
        version = branchPrefix + ".1"
    } else {
        def prefix = lastBranchTag.split("\\.").init().join(".")
        def suffix = Integer.parseInt(lastBranchTag.split("\\.").last()) + 1
        version = "${prefix}.${suffix}"
    }
    echo "Fix branch, next version is: ${version}"
}