package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.semantics.*

@Composable fun Equipment(s:GameState,vm:GameViewModel,store:Boolean=false){
    var selected by remember{mutableStateOf<String?>(null)};var filter by remember{mutableIntStateOf(0)}
    if(!store){
        AetherSurface(SurfaceKind.Artifact){
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Eyebrow("Câmara dos equipamentos")};RuneSeal(3,Gold,Modifier.size(28.dp))}
            BoxWithConstraints(Modifier.fillMaxWidth()){
                val portraitSize=(maxWidth-132.dp).coerceAtMost(190.dp)
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
                    Column(verticalArrangement=Arrangement.spacedBy(22.dp)){equipmentSlots.take(2).forEach{(kind,label)->EquipmentSlot(kind,label,s){selected=it}}}
                    Avatar(s.player!!,s,portraitSize)
                    Column(verticalArrangement=Arrangement.spacedBy(22.dp)){equipmentSlots.drop(2).take(2).forEach{(kind,label)->EquipmentSlot(kind,label,s){selected=it}}}
                }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){equipmentSlots.drop(4).forEach{(kind,label)->EquipmentSlot(kind,label,s){selected=it}}}
            MetricPair("${s.assets.count{it.equipped}}","equipados","${s.assets.count{a->shopItems.any{it.id==a.id}}}","na coleção")
            SagaDivider(color=OldBronze)
            Text("${s.assets.count{it.equipped}} equipados · ${s.assets.count{a->shopItems.any{it.id==a.id}}} artefatos na coleção",color=Muted,fontSize=11.sp)
            Text("Seus artefatos ocupam nichos ao redor do retrato. Toque em um item equipado para inspecionar.",color=Muted,fontSize=11.sp)
        }
    }else{
        Column(Modifier.fillMaxWidth().materialSurface(accent=OldBronze,leather=true).padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Eyebrow("O ARSENAL · MERCADOR DE RELÍQUIAS")
            Row(verticalAlignment=Alignment.CenterVertically){RelicArt("weapon",Gold,Modifier.size(76.dp));Column(Modifier.weight(1f)){Text("Entre couro e ferro",style=MaterialTheme.typography.titleLarge);Text("Artefatos para carregar a sua história.",color=Muted,fontSize=13.sp)}}
        }
    }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Eyebrow(if(store)"Espólios do mercador" else "Sua coleção");Text("◈ ${s.coins}",color=Gold,fontFamily=SagaFont,fontSize=20.sp)}
    ChipRow(listOf("Todos","Trajes","Relíquias","Companheiros"),filter,{filter=it})
    val items=shopItems.filter{item->(if(store)skillDefs.none{it.item==item.id}&&item.id!="mist-clasp" else s.assets.any{it.id==item.id})&&when(filter){1->item.kind in listOf("head","outfit");2->item.kind !in listOf("head","outfit","companion");3->item.kind=="companion";else->true}}
    if(items.isEmpty())Empty("Nenhuma relíquia nesta seleção. Conquiste fragmentos ou desperte uma habilidade para ampliar sua coleção.")
    items.chunked(2).forEach{row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)){
        row.forEach{item->val r=rarity(item);val color=rarityColor(r);val owned=s.assets.find{it.id==item.id}
            AetherSurface(SurfaceKind.Artifact,Modifier.weight(1f).clickable{selected=item.id}){
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){RuneSeal(r,color,Modifier.size(24.dp));Text(rarityNames[r].uppercase(),Modifier.weight(1f),color=color,fontSize=11.sp,letterSpacing=.7.sp)}
                Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){RelicArt(item.kind,color,Modifier.size(94.dp),item.id)}
                SagaDivider(color=color)
                Text(item.name,style=MaterialTheme.typography.titleMedium)
                Text(if(owned?.equipped==true)"◆ Equipado" else if(owned!=null)"Na coleção" else "◈ ${item.cost} fragmentos",color=color,fontSize=11.sp)
            }
        };if(row.size==1)Spacer(Modifier.weight(1f))
    }}
    if(!store)s.assets.filter{it.kind=="trophy"}.forEach{a->Panel{RuneSeal(4,Gold,Modifier.size(36.dp));Eyebrow("Troféu de missão");Text(s.quests.find{q->s.claims.any{it.questId==q.id&&"quest:${it.id}"==a.id}}?.special?:"Memória da jornada")}}
    selected?.let{id->val item=shopItems.first{it.id==id};val owned=s.assets.find{it.id==id};val color=rarityColor(rarity(item))
        AlertDialog(onDismissRequest={selected=null},title={Text(item.name,fontFamily=SagaFont)},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center){RelicArt(item.kind,color,Modifier.size(182.dp),item.id)}
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){RuneSeal(rarity(item),color,Modifier.size(28.dp));Eyebrow(rarityNames[rarity(item)],color)}
            Text(if(owned?.equipped==true)"Equipado" else if(owned!=null)"Na coleção" else "Disponível no arsenal",color=color,fontSize=12.sp)
            Text(item.description);SagaDivider(color=color);Eyebrow("Procedência",color);Text(itemOrigin(item),color=Muted,fontSize=13.sp);if(owned!=null)Text("Guardado na coleção em ${displayDate(owned.date)}",color=Muted,fontSize=12.sp)
        }},confirmButton={Action(if(owned!=null)if(owned.equipped)"Desequipar" else "Equipar" else "Adquirir · ${item.cost}",{vm.run{if(owned!=null)vm.repo.equip(owned) else vm.repo.buy(item);selected=null}},enabled=owned!=null||s.coins>=item.cost)},dismissButton={TextButton({selected=null}){Text("Fechar")}})
    }
}
@Composable private fun EquipmentSlot(kind:String,label:String,s:GameState,select:(String)->Unit){
    val a=s.assets.find{it.kind==kind&&it.equipped};val item=shopItems.find{it.id==a?.id};val color=if(item!=null)rarityColor(rarity(item)) else Muted
    Column(Modifier.width(62.dp).heightIn(min=72.dp).clip(CutCornerShape(5.dp)).background(color.copy(alpha=.08f)).border(if(item!=null)1.5.dp else 1.dp,color.copy(alpha=if(item!=null).65f else .3f),CutCornerShape(5.dp)).clickable(enabled=item!=null,onClickLabel="Detalhes de ${item?.name?:label}"){select(item!!.id)}.padding(5.dp).semantics(mergeDescendants=true){contentDescription="$label: ${item?.name?:"vazio"}"},horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(3.dp)){
        RelicArt(kind,color.copy(alpha=if(item==null).55f else 1f),Modifier.size(42.dp).alpha(if(item==null).22f else 1f),item?.id)
        Text(when(kind){"companion"->"Aliado";"weapon"->"Arma";"outfit"->"Traje";else->label},color=color,fontSize=9.sp,lineHeight=12.sp)
        Text(if(a==null)"Vazio" else "Equipado",color=color,fontSize=9.sp)
    }
}
@Composable fun SkillTree(s:GameState,vm:GameViewModel){
    Title("Constelações da alma","Escolhas cosméticas que acompanham sua jornada.")
    Panel{Stat("${skillPoints(s)}","PONTOS DISPONÍVEIS");Text("Um ponto a cada três níveis. Cada habilidade custa um ponto e concede uma relíquia equipável. Seus hábitos e estatísticas continuam livres.",color=Muted,fontSize=13.sp);Text("Se o XP for corrigido, habilidades obtidas permanecem; novos pontos aguardam a compensação do nível.",color=Muted,fontSize=11.sp)}
    skillDefs.groupBy{it.path}.forEach{(path,nodes)->
        Eyebrow(path)
        nodes.forEachIndexed{i,skill->
            if(i>0)SagaDivider(color=Blue)
            val owned=s.assets.any{it.id=="skill:${skill.id}"};val ready=s.level>=skill.level&&(skill.parent==null||s.assets.any{it.id=="skill:${skill.parent}"})&&skillPoints(s)>0
            Panel(accent=if(owned)Green else Violet){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){RelicArt(shopItems.first{it.id==skill.item}.kind,if(owned)Green else Violet,itemId=skill.item);Column(Modifier.weight(1f)){Text(skill.name,style=MaterialTheme.typography.titleLarge);Text("Nível ${skill.level} · 1 ponto",color=Muted,fontSize=12.sp);Text(if(owned)"Inscrita na constelação" else if(ready)"Disponível" else "Bloqueada",color=if(owned)Green else Muted,fontSize=12.sp)}};skill.parent?.let{parent->Text("Requer: ${skillDefs.first{it.id==parent}.name}",color=Muted,fontSize=12.sp)};Text(skill.description,color=Muted);Action(if(owned)"Despertada · item no inventário" else "Despertar habilidade",{vm.run{vm.repo.unlockSkill(skill.id)}},enabled=!owned&&ready)}
        }
    }
}

@Composable fun EquippedNiches(s:GameState){
    val items=s.assets.filter{it.equipped}.mapNotNull{a->shopItems.find{it.id==a.id}}
    if(items.isNotEmpty()){
        Eyebrow("Artefatos preparados")
        items.chunked(3).forEach{row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
            row.forEach{item->Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){RelicArt(item.kind,rarityColor(rarity(item)),Modifier.fillMaxWidth().aspectRatio(1f),item.id);Text(item.name,color=Muted,fontSize=10.sp,lineHeight=14.sp)}}
            repeat(3-row.size){Spacer(Modifier.weight(1f))}
        }}
    }
}
