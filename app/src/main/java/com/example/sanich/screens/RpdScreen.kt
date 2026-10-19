package com.example.sanich.screens

import com.example.sanich.CalculationHistoryViewModel
import com.example.sanich.HistoryEntry
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.PI

@Composable
fun RpdScreen(historyViewModel: CalculationHistoryViewModel, onBack:()->Unit){
 val historySessionKey = remember { java.util.UUID.randomUUID().toString() }
 var userEdited by remember { mutableStateOf(false) }
 var dCasing by remember{mutableStateOf("0.15")}; var dTubing by remember{mutableStateOf("0.073")}
 var rhoOil by remember{mutableStateOf("870")}; var rhoWater by remember{mutableStateOf("1015")}; var watercut by remember{mutableStateOf("80")}
 var inflow by remember{mutableStateOf("17")}; var pump by remember{mutableStateOf("57")}; var pIntake by remember{mutableStateOf("30")}; var run by remember{mutableStateOf("10")}; var maxAccum by remember{mutableStateOf("20")}
 fun v(x:String)=x.replace(',','.').toDoubleOrNull()
 val calc=runCatching{
  val D=v(dCasing)?:error("Введите D"); val d=v(dTubing)?:error("Введите d"); val rn=v(rhoOil)?:error("Введите плотность нефти"); val rw=v(rhoWater)?:error("Введите плотность воды")
  val b=(v(watercut)?:error("Введите обводнённость"))/100.0; val qin=v(inflow)?:error("Введите приток"); val qp=v(pump)?:error("Введите подачу ЭЦН"); val pp=v(pIntake)?:error("Введите Pпр"); val tr=v(run)?:error("Введите Tр")
  require(D>d&&d>0&&qin>0&&qp>qin&&tr>0){"Проверьте геометрию, приток, подачу и время"}
  val tmax=v(maxAccum)?:error("Введите ограничение времени накопления")
  require(tmax>0 && b in 0.0..1.0 && rn>0 && rw>0 && D>d && pp>0){"Проверьте исходные значения"}
  val initialVolume=(qp-qin)/1440.0*tr
  val initialAccum=initialVolume*1440.0/qin
  val actualRun=if (initialAccum>tmax) tr*tmax/initialAccum else tr
  val volume=(qp-qin)/1440.0*actualRun
  val accumulation=volume*1440.0/qin
  val area=PI/4.0*(D*D-d*d)
  val deltaLevel=volume/area
  val rho=rn*(1-b)+rw*b
  val deltaP=deltaLevel*rho*9.81/101300.0
  val pOff=pp-deltaP
  listOf(volume,accumulation,deltaLevel,rho,deltaP,pOff,pp,actualRun,initialAccum)
 }
 val currentHistoryEntry = calc.getOrNull()?.takeIf { values -> userEdited && values.all { it.isFinite() } }?.let { x ->
  fun f(z: Double) = String.format(Locale.US, "%.2f", z)
  HistoryEntry(title="Расчёт РПД", method="Периодическая откачка",
   result="Pоткл=${f(x[5])} ат; Pзап=${f(x[6])} ат",
   details="Tработы=${f(x[7])} мин; Tнакопления=${f(x[1])} мин; V=${f(x[0])} м³; ΔL=${f(x[2])} м; Qпр=$inflow м³/сут; Qэцн=$pump м³/сут")
 }
 val latestHistoryEntry by rememberUpdatedState(currentHistoryEntry)
 fun saveHistory() { latestHistoryEntry?.let { historyViewModel.saveSession(historySessionKey,it) } }
 PersistHistoryOnDispose(::saveHistory, debounceKey = currentHistoryEntry?.let { "$dCasing|$dTubing|$rhoOil|$rhoWater|$watercut|$inflow|$pump|$pIntake|$run|$maxAccum" })
 fun saveAndBack() { saveHistory(); onBack() }
 BackHandler(onBack = ::saveAndBack)
 Scaffold{pv->Column(Modifier.fillMaxSize().background(Color(0xFFF6F6F6)).padding(pv)){
  CalculationTopBar("Расчёт РПД",::saveAndBack)
  Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
   Text("Периодическая откачка по технологической инструкции",fontWeight=FontWeight.Bold)
   Text("Расчёт цикла откачки/накопления и ориентировочных уставок давления на приёме.")
   RpdField(dCasing,{dCasing=it; userEdited=true},"D внутренний ЭК, м"); RpdField(dTubing,{dTubing=it; userEdited=true},"d наружный НКТ, м")
   Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ RpdField(rhoOil,{rhoOil=it; userEdited=true},"ρ нефти, кг/м³",Modifier.weight(1f)); RpdField(rhoWater,{rhoWater=it; userEdited=true},"ρ воды, кг/м³",Modifier.weight(1f)) }
   RpdField(watercut,{watercut=it; userEdited=true},"Обводнённость B, %")
   Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ RpdField(inflow,{inflow=it; userEdited=true},"Qпр, м³/сут",Modifier.weight(1f)); RpdField(pump,{pump=it; userEdited=true},"Qэцн, м³/сут",Modifier.weight(1f)) }
   RpdField(pIntake,{pIntake=it; userEdited=true},"Проектное Pпр, ат"); RpdField(run,{run=it; userEdited=true},"Исходное время работы Tр, мин")
   RpdField(maxAccum,{maxAccum=it; userEdited=true},"Максимальное время накопления, мин")
   val x=calc.getOrNull()
   Card(colors=CardDefaults.cardColors(containerColor=if(x!=null) Color(0xFFFFE082) else Color.White)){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
    Text("Результат",fontWeight=FontWeight.Bold)
    if(x==null) Text(calc.exceptionOrNull()?.message?:"Заполните данные") else { fun f(z:Double)=String.format(Locale.US,"%.2f",z)
     Text("Время работы Tр: ${f(x[7])} мин",fontWeight=FontWeight.Bold); Text("Объём откачки V: ${f(x[0])} м³"); Text("Время накопления Tн: ${f(x[1])} мин"); Text("Изменение уровня ΔL: ${f(x[2])} м")
     Text("Расчётная плотность жидкости: ${f(x[3])} кг/м³"); Text("Перепад ΔPпр: ${f(x[4])} ат")
     Text("Давление отключения Pоткл: ${f(x[5])} ат",fontWeight=FontWeight.Bold); Text("Давление запуска Pзап: ${f(x[6])} ат",fontWeight=FontWeight.Bold)
    }
   }}
   Text("Формулы",fontWeight=FontWeight.Bold); Formula("V = ((Qэцн − Qпр) × Tр) / 1440"); Formula("Tн = V × 1440 / Qпр"); Formula("Tр’ = (Tр × Tн.max) / Tн") ; Formula("ΔL = (4 × V) / (π × (D² − d²))"); Formula("ΔPпр = (ΔL × ρж × g) / 101300"); Formula("Pоткл = Pпр − ΔPпр   •   Pзап = Pпр")
   Text("Контрольный пример регламента: Tр ≈ 8,5 мин, Tн = 20 мин, Pоткл ≈ 28,3 ат, Pзап = 30 ат.",style=MaterialTheme.typography.bodySmall)
   Text("Важно: итоговые уставки должны проверяться по действующей редакции технологического регламента и фактической конфигурации ТМС/СУ. В регламенте задержка АПВ для периодических режимов задаётся с учётом времени накопления и отдельного минимального ограничения.",style=MaterialTheme.typography.bodySmall)
  }
 }}
}
@Composable private fun RpdField(v:String,on:(String)->Unit,label:String,m:Modifier=Modifier.fillMaxWidth())=OutlinedTextField(v,{on(it.filter{c->c.isDigit()||c=='.'||c==','})},label={Text(label)},modifier=m,singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))
@Composable private fun Formula(s:String)=Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFFFF3C4))){FormulaDisplay(s)}
