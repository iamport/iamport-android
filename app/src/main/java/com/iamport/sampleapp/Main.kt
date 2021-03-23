import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

var result = 0
fun main(): Unit = with(BufferedReader(InputStreamReader(System.`in`))) {

    val token = StringTokenizer(this.readLine())
    val T = Integer.parseInt(token.nextToken())

    val A = IntArray(T) { Int.MAX_VALUE }
    val B = IntArray(T) { Int.MAX_VALUE }
    val C = IntArray(T) { Int.MAX_VALUE }
    val D = IntArray(T) { Int.MAX_VALUE }

    val FIRST = IntArray((T * T)) { Int.MAX_VALUE }
    val SECOND = IntArray((T * T)) { Int.MAX_VALUE }
    val LIST = mutableListOf<Int>()

    for (I in 0 until T) {
        val arr = readLine().split(' ').map { it.toInt() }
        A[I] = arr[0]
        B[I] = arr[1]
        C[I] = arr[2]
        D[I] = arr[3]
    }

//    var result = 0
    var cnt = 0

    for (I in 0 until T) {
        for (J in 0 until T) {

            FIRST[cnt] = A[I] + B[J]
            SECOND[cnt] = C[I] + D[J]
            LIST.add(C[I] + D[J])

            cnt++
        }
    }

//    FIRST.sort()
    SECOND.sort()
    LIST.sort()
//
//    FIRST.forEach {
//        print("$it, ")
//    }
//    println()
//    SECOND.forEach {
//        print("$it, ")
//    }
//
//    println()

    for (I in FIRST.indices) {
        if (binarySearch(LIST, -FIRST[I]) > -1) {
            result++
        }
    }


    print(result)

    this.close()
}


fun binarySearch(arr: MutableList<Int>, target: Int): Int {
    var low = 0
    var high = arr.lastIndex
    var mid = 0

    while (low <= high) {
        mid = (low + high) / 2

        when {
            arr[mid] == target -> {
                return 0
            }
            arr[mid] > target -> high = mid - 1
            else -> low = mid + 1
        }
    }
    return -1
}