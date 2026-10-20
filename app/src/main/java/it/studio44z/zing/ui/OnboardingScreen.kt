package it.studio44z.zing.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.studio44z.zing.R
import java.util.Locale
import kotlinx.coroutines.delay

internal data class OnboardingCopy(
    val titles: List<String>,
    val descriptions: List<String>,
    val skip: String,
    val back: String,
    val next: String,
    val start: String,
    val scan: String,
    val quantity: String,
    val expiry: String,
    val available: String,
    val missing: String,
    val shoppingItemOne: String,
    val shoppingItemTwo: String,
    val reviewTitle: String,
    val reviewDescription: String,
    val reviewButton: String,
) {
    companion object {
        fun forLanguage(languageCode: String): OnboardingCopy = when (languageCode.lowercase(Locale.ROOT).take(2)) {
            "en" -> OnboardingCopy(
                titles = listOf("Welcome to Zing", "Organize your pantry", "Discover what to cook", "Plan your shopping"),
                descriptions = listOf(
                    "Your everyday companion for keeping ingredients, pantry and cooking in order.",
                    "Add products in seconds, scan barcodes and keep quantities and expiration dates under control.",
                    "Explore built-in recipes and online ideas, then see which ingredients you already have and which are missing.",
                    "Keep the ingredients you need in one handy list and make everyday planning easier.",
                ),
                skip = "Skip", back = "Back", next = "Next", start = "Get started",
                scan = "Scan a barcode", quantity = "Track quantities", expiry = "Watch expiration dates",
                available = "You have", missing = "To buy", shoppingItemOne = "Fresh vegetables", shoppingItemTwo = "Pasta and rice",
                reviewTitle = "Meet Zing again", reviewDescription = "Reopen the short introduction whenever you need a refresher.", reviewButton = "View introduction",
            )
            "es" -> OnboardingCopy(
                titles = listOf("Te damos la bienvenida a Zing", "Organiza tu despensa", "Descubre qué cocinar", "Planifica la compra"),
                descriptions = listOf(
                    "Tu compañero diario para organizar la despensa, los ingredientes y la cocina.",
                    "Añade productos rápidamente, escanea códigos de barras y controla cantidades y caducidades.",
                    "Explora recetas incluidas e ideas en línea y descubre qué ingredientes tienes y cuáles te faltan.",
                    "Reúne en una sola lista los ingredientes que necesitas y simplifica la planificación diaria.",
                ),
                skip = "Saltar", back = "Atrás", next = "Siguiente", start = "Empezar",
                scan = "Escanear código", quantity = "Controlar cantidades", expiry = "Revisar caducidades",
                available = "Disponible", missing = "Por comprar", shoppingItemOne = "Verduras frescas", shoppingItemTwo = "Pasta y arroz",
                reviewTitle = "Vuelve a conocer Zing", reviewDescription = "Puedes volver a abrir esta breve presentación cuando quieras.", reviewButton = "Ver presentación",
            )
            "fr" -> OnboardingCopy(
                titles = listOf("Bienvenue dans Zing", "Organisez votre placard", "Découvrez quoi cuisiner", "Préparez vos courses"),
                descriptions = listOf(
                    "Votre compagnon au quotidien pour organiser les ingrédients, les réserves et la cuisine.",
                    "Ajoutez rapidement des produits, scannez les codes-barres et suivez quantités et dates limites.",
                    "Parcourez les recettes intégrées et les idées en ligne, puis repérez les ingrédients disponibles ou manquants.",
                    "Rassemblez les ingrédients nécessaires dans une seule liste et simplifiez l'organisation quotidienne.",
                ),
                skip = "Passer", back = "Retour", next = "Suivant", start = "C'est parti",
                scan = "Scanner un code-barres", quantity = "Suivre les quantités", expiry = "Suivre les dates limites",
                available = "Disponible", missing = "À acheter", shoppingItemOne = "Légumes frais", shoppingItemTwo = "Pâtes et riz",
                reviewTitle = "Redécouvrez Zing", reviewDescription = "Vous pouvez rouvrir cette courte présentation à tout moment.", reviewButton = "Voir la présentation",
            )
            "de" -> OnboardingCopy(
                titles = listOf("Willkommen bei Zing", "Vorrat organisieren", "Entdecke Kochideen", "Einkäufe planen"),
                descriptions = listOf(
                    "Dein täglicher Begleiter, um Vorräte, Zutaten und Kochen im Blick zu behalten.",
                    "Füge Produkte schnell hinzu, scanne Barcodes und behalte Mengen und Haltbarkeit im Blick.",
                    "Entdecke integrierte Rezepte und Online-Ideen und erkenne, welche Zutaten vorhanden sind oder fehlen.",
                    "Sammle benötigte Zutaten in einer praktischen Liste und erleichtere deine tägliche Planung.",
                ),
                skip = "Überspringen", back = "Zurück", next = "Weiter", start = "Los geht's",
                scan = "Barcode scannen", quantity = "Mengen verwalten", expiry = "Haltbarkeit prüfen",
                available = "Vorhanden", missing = "Zu kaufen", shoppingItemOne = "Frisches Gemüse", shoppingItemTwo = "Nudeln und Reis",
                reviewTitle = "Lerne Zing erneut kennen", reviewDescription = "Du kannst diese kurze Einführung jederzeit erneut öffnen.", reviewButton = "Einführung ansehen",
            )
            "pt" -> OnboardingCopy(
                titles = listOf("Bem-vindo ao Zing", "Organize a despensa", "Descubra o que cozinhar", "Planeie as compras"),
                descriptions = listOf(
                    "O seu companheiro diário para organizar a despensa, os ingredientes e a cozinha.",
                    "Adicione produtos rapidamente, leia códigos de barras e acompanhe quantidades e validades.",
                    "Explore receitas incluídas e sugestões online e descubra os ingredientes disponíveis e em falta.",
                    "Reúna os ingredientes necessários numa lista prática e facilite o planeamento do dia a dia.",
                ),
                skip = "Saltar", back = "Voltar", next = "Seguinte", start = "Começar",
                scan = "Ler código de barras", quantity = "Controlar quantidades", expiry = "Verificar validades",
                available = "Disponível", missing = "A comprar", shoppingItemOne = "Legumes frescos", shoppingItemTwo = "Massa e arroz",
                reviewTitle = "Conheça o Zing novamente", reviewDescription = "Pode voltar a abrir esta breve apresentação sempre que precisar.", reviewButton = "Ver apresentação",
            )
            else -> OnboardingCopy(
                titles = listOf("Benvenuto in Zing", "Organizza la dispensa", "Scopri cosa cucinare", "Pianifica la spesa"),
                descriptions = listOf(
                    "Il tuo compagno quotidiano per organizzare dispensa, ingredienti e cucina.",
                    "Aggiungi i prodotti in pochi secondi, scansiona i codici a barre e tieni sotto controllo quantità e scadenze.",
                    "Esplora le ricette integrate e le idee online, scoprendo quali ingredienti hai già e quali mancano.",
                    "Raccogli in una lista pratica gli ingredienti che ti servono e semplifica l'organizzazione quotidiana.",
                ),
                skip = "Salta", back = "Indietro", next = "Avanti", start = "Inizia",
                scan = "Scansiona un codice", quantity = "Controlla le quantità", expiry = "Tieni d'occhio le scadenze",
                available = "Disponibili", missing = "Da comprare", shoppingItemOne = "Verdure fresche", shoppingItemTwo = "Pasta e riso",
                reviewTitle = "Rivedi la guida di Zing", reviewDescription = "Puoi riaprire questa breve presentazione dalle Impostazioni quando vuoi.", reviewButton = "Rivedi presentazione",
            )
        }
    }
}

@Composable
internal fun OnboardingScreen(
    langCode: String,
    onFinish: () -> Unit,
) {
    val copy = remember(langCode) { OnboardingCopy.forLanguage(langCode) }
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    var pageTransitioning by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(currentStep) {
        // The transition is 220 ms; keep controls briefly disabled to avoid queued/double taps.
        delay(240)
        pageTransitioning = false
    }

    BackHandler(enabled = currentStep > 0 && !pageTransitioning) {
        pageTransitioning = true
        currentStep = (currentStep - 1).coerceAtLeast(0)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Zing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (currentStep < 3) {
                        TextButton(onClick = onFinish) { Text(copy.skip) }
                    } else {
                        Spacer(Modifier.width(64.dp))
                    }
                }

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(tween(220)) { it / 5 } + fadeIn(tween(220))) togetherWith
                                (slideOutHorizontally(tween(180)) { -it / 5 } + fadeOut(tween(180)))
                        } else {
                            (slideInHorizontally(tween(220)) { -it / 5 } + fadeIn(tween(220))) togetherWith
                                (slideOutHorizontally(tween(180)) { it / 5 } + fadeOut(tween(180)))
                        }
                    },
                    label = "zing-onboarding-page-transition",
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) { step ->
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        OnboardingIllustration(step = step, copy = copy)
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = copy.titles[step],
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = copy.descriptions[step],
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            repeat(4) { index ->
                                Box(
                                    modifier = Modifier
                                        .width(if (index == step) 24.dp else 8.dp)
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == step) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant,
                                        ),
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = {
                                if (!pageTransitioning) {
                                    pageTransitioning = true
                                    currentStep--
                                }
                            },
                            enabled = !pageTransitioning,
                            modifier = Modifier.weight(1f),
                        ) { Text(copy.back) }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    Button(
                        onClick = {
                            if (currentStep == 3) {
                                onFinish()
                            } else if (!pageTransitioning) {
                                pageTransitioning = true
                                currentStep++
                            }
                        },
                        enabled = !pageTransitioning,
                        modifier = Modifier.weight(1.25f),
                    ) {
                        Text(if (currentStep == 3) copy.start else copy.next)
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingIllustration(
    step: Int,
    copy: OnboardingCopy,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(218.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (step) {
                0 -> Image(
                    painter = painterResource(R.drawable.zing_icon_master),
                    contentDescription = null,
                    modifier = Modifier.size(142.dp).clip(RoundedCornerShape(30.dp)),
                )
                1 -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    IllustrationFeature(Icons.Outlined.QrCodeScanner, copy.scan)
                    IllustrationFeature(Icons.Outlined.Inventory2, copy.quantity)
                    IllustrationFeature(Icons.Outlined.CalendarMonth, copy.expiry)
                }
                2 -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    IllustrationTile(
                        icon = Icons.Outlined.CheckCircle,
                        label = copy.available,
                        modifier = Modifier.weight(1f),
                    )
                    IllustrationTile(
                        icon = Icons.Outlined.ShoppingCart,
                        label = copy.missing,
                        modifier = Modifier.weight(1f),
                    )
                }
                else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IllustrationShoppingRow(copy.shoppingItemOne, checked = true)
                    IllustrationShoppingRow(copy.shoppingItemTwo, checked = false)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingBasket,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IllustrationFeature(
    icon: ImageVector,
    label: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun IllustrationTile(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(126.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
        Spacer(Modifier.height(12.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun IllustrationShoppingRow(
    label: String,
    checked: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(21.dp),
        )
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
