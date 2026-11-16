package br.com.ascend.aetheris

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun readLocalBackup(context:Context,uri:Uri):GameState=withContext(Dispatchers.IO){
    val bytes=context.contentResolver.openInputStream(uri)?.use{input->
        val output=java.io.ByteArrayOutputStream();val buffer=ByteArray(8192)
        while(true){val count=input.read(buffer);if(count<0)break;require(output.size()+count<=20_000_000){"Backup maior que 20 MB."};output.write(buffer,0,count)}
        output.toByteArray()
    }?:throw java.io.IOException("Não foi possível ler o arquivo.")
    Backup.decode(bytes.toString(Charsets.UTF_8))
}
