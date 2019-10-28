def tag = "16255.6700.15"
println tag.split("\\.").init().join(".")

println tag.split("\\.").eachWithIndex { token, index -> println "$token as position $index" }
def prefix = tag.split("\\.").init().join(".")
def suffix = Integer.parseInt(tag.split("\\.").last()) + 1

println "Next version is ${prefix}.${suffix}"

