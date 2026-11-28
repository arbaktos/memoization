# The Lingvanex API's requests and responses are read and written by Gson, which goes by field
# names, and the Retrofit interface is found by reflection: neither may be renamed or removed.
-keep class ru.vasilisasycheva.translation.api.** { *; }
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

# Retrofit 2.9 predates R8's full mode, AGP 8's default, which strips the generic signatures a
# suspend function returning Response<T> is read through. These are the rules Retrofit 2.10 ships.
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
