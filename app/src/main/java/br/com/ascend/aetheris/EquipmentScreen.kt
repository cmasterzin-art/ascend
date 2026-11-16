package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*

@Composable fun Equipment(s:GameState,vm:GameViewModel,store:Boolean=false){
    var selected by remember{mutableStateOf<String?>(null)};var filter by remember{mutableIntStateOf(0)}
    Eyebrow(if(store)"Mercador de relíquias" else "Câmara dos equipamentos")
    if(!store)Panel{
        BoxWithConstraints(Modifier.fillMaxWidth()) {
        val portraitSize=(maxWidth-150.dp).coerceAtMost(128.dp)
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceEvenly){
            Column{equipmentSlots.take(2).forEach{(kind,label)->EquipmentSlot(kind,label,s){selected=it}}}
            Avatar(s.player!!,s,portraitSize)
            Column{equipmentSlots.drop(2).take(2).forEach{(kind,label)->EquipmentSlot(kind,label,s){selected=it}}}
        }
        }
        Text("${s.assets.count{it.equipped}} itens equipados · ${s.assets.count{a->shopItems.any{it.id==a.id}}} relíquias",color=Gold,fontSize=12.sp)
    }
    Text("◈ ${s.coins} Fragmentos de Aether",color=Gold)
    ChipRow(listOf("Todos","Trajes","Relíquias","Companheiros"),filter,{filter=it})
    val items=shopItems.filter{item->(if(store)skillDefs.none{it.item==item.id} else s.assets.any{it.id==item.id})&&when(filter){1->item.kind in listOf("head","outfit");2->item.kind !in listOf("head","outfit","companion");3->item.kind=="companion";else->true}}
    if(items.isEmpty())Empty("Nenhuma relíquia nesta seleção. Conquiste fragmentos ou desperte uma habilidade para ampliar sua coleção.")
    items.chunked(2).forEach{row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)){
        row.forEach{item->val r=rarity(item);val color=rarityColor(r);val owned=s.assets.find{it.id==item.id}
            Column(Modifier.weight(1f).border(1.dp,color.copy(alpha=.6f),RoundedCornerShape(8.dp)).background(color.copy(alpha=.06f),RoundedCornerShape(8.dp)).clickable{selected=item.id}.padding(14.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(7.dp)){
                RelicArt(item.kind,color);Eyebrow(rarityNames[r],color);Text(item.name,style=MaterialTheme.typography.titleMedium);Text(if(owned?.equipped==true)"Equipado" else if(owned!=null)"Na coleção" else "◈ ${item.cost}",color=color,fontSize=12.sp)
            }
        };if(row.size==1)Spacer(Modifier.weight(1f))
    }}
    if(!store)s.assets.filter{it.kind=="trophy"}.forEach{a->Panel{Eyebrow("Troféu de missão");Text(s.quests.find{q->s.claims.any{it.questId==q.id&&"quest:${it.id}"==a.id}}?.special?:"Memória da jornada")}}
    selected?.let{id->val item=shopItems.first{it.id==id};val owned=s.assets.find{it.id==id};val color=rarityColor(rarity(item))
        AlertDialog(onDismissRequest={selected=null},title={Text(item.name)},text={Column(verticalArrangement=Arrangement.spacedBy(14.dp)){RelicArt(item.kind,color,Modifier.size(90.dp));Eyebrow(rarityNames[rarity(item)],color);Text(item.description);Text(itemOrigin(item),color=Muted);if(owned!=null)Text("Obtido em ${owned.date}",color=Muted,fontSize=12.sp)}},confirmButton={TextButton({vm.run{if(owned!=null)vm.repo.equip(owned) else vm.repo.buy(item);selected=null}},enabled=owned!=null||s.coins>=item.cost){Text(if(owned!=null)if(owned.equipped)"Desequipar" else "Equipar" else "Adquirir · ${item.cost}")}},dismissButton={TextButton({selected=null}){Text("Fechar")}})
    }
}
@Composable private fun EquipmentSlot(kind:String,label:String,s:GameState,select:(String)->Unit){
    val a=s.assets.find{it.kind==kind&&it.equipped};val item=shopItems.find{it.id==a?.id};val color=if(item!=null)rarityColor(rarity(item)) else Muted
    Column(Modifier.width(75.dp).padding(vertical=6.dp).border(1.dp,color.copy(alpha=.5f),RoundedCornerShape(8.dp)).clickable(enabled=item!=null){select(item!!.id)}.padding(6.dp),horizontalAlignment=Alignment.CenterHorizontally){RelicArt(kind,color,Modifier.size(38.dp));Text(label,color=color,fontSize=10.sp);Text(if(a==null)"Vazio" else "Equipado",color=Muted,fontSize=9.sp)}
}
@Composable fun SkillTree(s:GameState,vm:GameViewModel){
    Title("Constelações da alma","Escolhas cosméticas que acompanham sua jornada.")
    Panel{Stat("${skillPoints(s)}","PONTOS DISPONÍVEIS");Text("Um ponto a cada três níveis. Cada habilidade custa um ponto e concede uma relíquia equipável. Seus hábitos e estatísticas continuam livres.",color=Muted,fontSize=13.sp);Text("Se o XP for corrigido, habilidades obtidas permanecem; novos pontos aguardam a compensação do nível.",color=Muted,fontSize=11.sp)}
    skillDefs.groupBy{it.path}.forEach{(path,nodes)->
        Eyebrow(path)
        nodes.forEachIndexed{i,skill->
            if(i>0)Text("│",Modifier.padding(start=27.dp),color=Gold,fontSize=26.sp)
            val owned=s.assets.any{it.id=="skill:${skill.id}"};val ready=s.level>=skill.level&&(skill.parent==null||s.assets.any{it.id=="skill:${skill.parent}"})&&skillPoints(s)>0
            Panel(accent=if(owned)Green else Violet){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){RelicArt(shopItems.first{it.id==skill.item}.kind,if(owned)Green else Violet);Column(Modifier.weight(1f)){Text(skill.name,style=MaterialTheme.typography.titleLarge);Text("Nível ${skill.level} · 1 ponto",color=Muted,fontSize=12.sp)}};Text(skill.description,color=Muted);Action(if(owned)"Despertada · item no inventário" else "Despertar habilidade",{vm.run{vm.repo.unlockSkill(skill.id)}},enabled=!owned&&ready)}
        }
    }
}
