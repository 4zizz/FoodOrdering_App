package com.example.foodordering

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

data class FoodItem(
    val id: Int,
    val name: String,
    val price: Int,
    val category: String,
    val description: String,
    val emoji: String,
    val rating: Double,
    val calories: Int,
    val minutes: Int,
    val ingredients: List<Pair<String, String>>
) {
    val imageName: String
        get() = "menu_" + name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
}

data class CartItem(
    val food: FoodItem,
    val quantity: Int,
    val size: String = "M"
) {
    val key: String get() = "${food.id}-$size"
    val unitPrice: Int get() = food.price + sizeExtra(size)
    val total: Int get() = unitPrice * quantity
}

fun sizeExtra(size: String): Int = when (size) {
    "S" -> -2000
    "L" -> 3000
    else -> 0
}

val dummyFoods = listOf(
    FoodItem(
        1, "Bakmi GM Pangsit Goreng", 38000, "Makanan",
        "Bakmi dengan topping ayam dan pangsit goreng renyah.", "🍜", 4.8, 520, 15,
        listOf("🍜" to "Bakmi", "🍗" to "Ayam", "🥟" to "Pangsit", "🥬" to "Sawi", "🧅" to "Bawang")
    ),
    FoodItem(
        2, "Nasi Goreng Smoked Chicken Reguler", 42000, "Makanan",
        "Nasi goreng dengan potongan smoked chicken yang gurih.", "🍛", 4.7, 560, 15,
        listOf("🍚" to "Nasi", "🍗" to "Smoked Chicken", "🥚" to "Telur", "🧅" to "Bawang", "🌶️" to "Cabai")
    ),
    FoodItem(
        3, "Kwetiau Goreng Ayam", 36000, "Makanan",
        "Kwetiau goreng dengan ayam, telur, dan sayuran segar.", "🍝", 4.6, 480, 12,
        listOf("🍝" to "Kwetiau", "🍗" to "Ayam", "🥚" to "Telur", "🥬" to "Sawi", "🧄" to "Bawang Putih")
    ),
    FoodItem(
        4, "Nasi Cap Cay", 38000, "Makanan",
        "Nasi dengan tumisan cap cay sayuran dan kuah gurih.", "🥘", 4.5, 430, 15,
        listOf("🍚" to "Nasi", "🥦" to "Brokoli", "🥕" to "Wortel", "🍄" to "Jamur", "🧄" to "Bawang Putih")
    ),
    FoodItem(
        5, "Lemon Es Teh", 18000, "Minuman",
        "Teh dingin segar dengan perasan lemon.", "🍋", 4.5, 90, 3,
        listOf("🍃" to "Teh", "🍋" to "Lemon", "🧊" to "Es Batu", "🍬" to "Gula")
    ),
    FoodItem(
        6, "Iced Lychee Tea", 22000, "Minuman",
        "Teh dingin dengan rasa leci yang manis dan segar.", "🧋", 4.6, 110, 3,
        listOf("🍃" to "Teh", "🍒" to "Leci", "🧊" to "Es Batu", "🍬" to "Gula")
    ),
    FoodItem(
        7, "Pangsit Goreng", 25000, "Cemilan",
        "Pangsit goreng renyah dengan isian ayam.", "🥟", 4.6, 300, 8,
        listOf("🥟" to "Kulit Pangsit", "🍗" to "Ayam", "🧄" to "Bawang Putih", "🛢️" to "Minyak")
    ),
    FoodItem(
        8, "Siomay Goreng", 27000, "Cemilan",
        "Siomay goreng garing dengan isian ayam dan udang.", "🍤", 4.5, 320, 10,
        listOf("🍤" to "Udang", "🍗" to "Ayam", "🥟" to "Kulit Siomay", "🧄" to "Bawang Putih")
    )
)

enum class Screen { HOME, CART, FAVORITE, CHECKOUT, THEME }

const val ONGKIR = 5000

fun rupiah(value: Int): String = "Rp" + "%,d".format(Locale("id", "ID"), value)

private val LightColors = lightColorScheme(
    primary = Color(0xFFF4C430),
    onPrimary = Color(0xFF3A2A00),
    primaryContainer = Color(0xFFFFEFB8),
    onPrimaryContainer = Color(0xFF3A2A00),
    secondaryContainer = Color(0xFFFCEBB0),
    tertiary = Color(0xFFB3261E),
    tertiaryContainer = Color(0xFFFFE0DA),
    onTertiaryContainer = Color(0xFF410002),
    background = Color(0xFFFFFBF0),
    surface = Color(0xFFFFFDF7),
    surfaceVariant = Color(0xFFF3EACB)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFD54F),
    onPrimary = Color(0xFF3A2A00),
    primaryContainer = Color(0xFF5A4500),
    onPrimaryContainer = Color(0xFFFFEFB8),
    secondaryContainer = Color(0xFF4A3D14),
    tertiary = Color(0xFFFFB4A8),
    tertiaryContainer = Color(0xFF5C1A12),
    onTertiaryContainer = Color(0xFFFFE0DA),
    background = Color(0xFF181408),
    surface = Color(0xFF1F1A0C),
    surfaceVariant = Color(0xFF383219)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }
            var started by rememberSaveable { mutableStateOf(false) }

            MaterialTheme(colorScheme = if (isDarkTheme) DarkColors else LightColors) {
                if (!started) {
                    WelcomeScreen(onStart = { started = true })
                } else {
                    FoodOrderApp(
                        isDarkTheme = isDarkTheme,
                        onThemeChange = { isDarkTheme = it }
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    val primary = MaterialTheme.colorScheme.tertiary
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            BrandLogo(modifier = Modifier.size(280.dp))
        }

        Text(
            text = buildAnnotatedString {
                append("Bringing ")
                withStyle(SpanStyle(color = primary)) { append("People") }
                append(" Together")
            },
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Get Started", fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodOrderApp(isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {
    var currentScreen by rememberSaveable { mutableStateOf(Screen.HOME) }
    var selectedFoodId by rememberSaveable { mutableStateOf<Int?>(null) }
    val selectedFood = dummyFoods.find { it.id == selectedFoodId }

    val cartItems = remember { mutableStateListOf<CartItem>() }
    val favorites = remember { mutableStateListOf<Int>() }
    val cartCount = cartItems.sumOf { it.quantity }

    var showDialog by remember { mutableStateOf(false) }
    var addedFoodName by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun addToCart(food: FoodItem, size: String, qty: Int) {
        val index = cartItems.indexOfFirst { it.food.id == food.id && it.size == size }
        if (index >= 0) {
            cartItems[index] = cartItems[index].copy(quantity = cartItems[index].quantity + qty)
        } else {
            cartItems.add(CartItem(food, qty, size))
        }
        addedFoodName = food.name
        showDialog = true
    }

    fun changeQuantity(key: String, delta: Int) {
        val index = cartItems.indexOfFirst { it.key == key }
        if (index >= 0) {
            val newQty = cartItems[index].quantity + delta
            if (newQty <= 0) cartItems.removeAt(index)
            else cartItems[index] = cartItems[index].copy(quantity = newQty)
        }
    }

    fun removeFromCart(key: String) {
        cartItems.removeAll { it.key == key }
    }

    fun toggleFavorite(id: Int) {
        if (favorites.contains(id)) favorites.remove(id) else favorites.add(id)
    }

    fun navigateTo(screen: Screen) {
        selectedFoodId = null
        currentScreen = screen
        scope.launch { drawerState.close() }
    }

    BackHandler(enabled = selectedFood != null || currentScreen != Screen.HOME) {
        when {
            selectedFood != null -> selectedFoodId = null
            currentScreen == Screen.CHECKOUT -> currentScreen = Screen.CART
            else -> currentScreen = Screen.HOME
        }
    }

    val showTopBar = selectedFood == null && currentScreen != Screen.HOME
    val showBottomBar = selectedFood == null && currentScreen != Screen.CHECKOUT

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .statusBarsPadding()
                        .padding(24.dp)
                ) {
                    Text("🍜", fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "GMOrder",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        "Pesan makanan favoritmu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    label = { Text("Beranda") },
                    icon = { Icon(Icons.Default.Home, null) },
                    selected = currentScreen == Screen.HOME && selectedFood == null,
                    onClick = { navigateTo(Screen.HOME) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Keranjang") },
                    icon = { Icon(Icons.Default.ShoppingCart, null) },
                    badge = { if (cartCount > 0) Text("$cartCount") },
                    selected = currentScreen == Screen.CART,
                    onClick = { navigateTo(Screen.CART) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Favorit") },
                    icon = { Icon(Icons.Default.Favorite, null) },
                    badge = { if (favorites.isNotEmpty()) Text("${favorites.size}") },
                    selected = currentScreen == Screen.FAVORITE,
                    onClick = { navigateTo(Screen.FAVORITE) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Tema Aplikasi") },
                    icon = { Icon(Icons.Default.Settings, null) },
                    selected = currentScreen == Screen.THEME,
                    onClick = { navigateTo(Screen.THEME) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (showTopBar) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                when (currentScreen) {
                                    Screen.CHECKOUT -> "Checkout"
                                    Screen.THEME -> "Pengaturan"
                                    Screen.CART -> "Keranjang"
                                    Screen.FAVORITE -> "Favorit"
                                    else -> "GMOrder"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            if (currentScreen == Screen.CHECKOUT) {
                                IconButton(onClick = { currentScreen = Screen.CART }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            }
                        },
                        actions = {
                            if (currentScreen != Screen.CART && currentScreen != Screen.CHECKOUT) {
                                IconButton(onClick = { navigateTo(Screen.CART) }) {
                                    BadgedBox(badge = {
                                        if (cartCount > 0) Badge { Text("$cartCount") }
                                    }) {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = "Keranjang")
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        NavigationBarItem(
                            selected = currentScreen == Screen.HOME,
                            onClick = { navigateTo(Screen.HOME) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                            label = { Text("Beranda") }
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.CART,
                            onClick = { navigateTo(Screen.CART) },
                            icon = {
                                BadgedBox(badge = {
                                    if (cartCount > 0) Badge { Text("$cartCount") }
                                }) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Keranjang")
                                }
                            },
                            label = { Text("Keranjang") }
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.FAVORITE,
                            onClick = { navigateTo(Screen.FAVORITE) },
                            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorit") },
                            label = { Text("Favorit") }
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.THEME,
                            onClick = { navigateTo(Screen.THEME) },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                            label = { Text("Pengaturan") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when {
                    selectedFood != null -> FoodDetailScreen(
                        food = selectedFood,
                        isFavorite = favorites.contains(selectedFood.id),
                        onToggleFavorite = { toggleFavorite(selectedFood.id) },
                        onBack = { selectedFoodId = null },
                        onAddToCart = { size, qty -> addToCart(selectedFood, size, qty) }
                    )

                    currentScreen == Screen.HOME -> HomeScreen(
                        foodList = dummyFoods,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onItemSelected = { selectedFoodId = it.id },
                        onQuickAdd = { addToCart(it, "M", 1) }
                    )

                    currentScreen == Screen.CART -> CartScreen(
                        cartItems = cartItems,
                        onIncrease = { changeQuantity(it, 1) },
                        onDecrease = { changeQuantity(it, -1) },
                        onRemove = { removeFromCart(it) },
                        onBrowse = { currentScreen = Screen.HOME },
                        onCheckout = { currentScreen = Screen.CHECKOUT }
                    )

                    currentScreen == Screen.FAVORITE -> FavoriteScreen(
                        foods = dummyFoods.filter { favorites.contains(it.id) },
                        onItemSelected = { selectedFoodId = it.id },
                        onToggleFavorite = { toggleFavorite(it) },
                        onBrowse = { currentScreen = Screen.HOME }
                    )

                    currentScreen == Screen.CHECKOUT -> CheckoutScreen(
                        cartItems = cartItems,
                        onOrderPlaced = { customerName ->
                            cartItems.clear()
                            currentScreen = Screen.HOME
                            scope.launch {
                                snackbarHostState.showSnackbar("Pesanan $customerName berhasil dibuat!")
                            }
                        }
                    )

                    currentScreen == Screen.THEME -> ThemeScreen(
                        isDarkTheme = isDarkTheme,
                        onThemeChange = onThemeChange
                    )
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check, null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                title = { Text("Berhasil Ditambahkan!") },
                text = {
                    Text(
                        "$addedFoodName telah dimasukkan ke keranjang belanja.",
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        showDialog = false
                        selectedFoodId = null
                        currentScreen = Screen.CART
                    }) { Text("Lihat Keranjang") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        selectedFoodId = null
                        currentScreen = Screen.HOME
                    }) { Text("Lanjut Belanja") }
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    foodList: List<FoodItem>,
    onMenuClick: () -> Unit,
    onItemSelected: (FoodItem) -> Unit,
    onQuickAdd: (FoodItem) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Semua") }
    val categories = listOf("Semua" to "🍽️", "Makanan" to "🍜", "Minuman" to "🥤", "Cemilan" to "🥟")
    val cardColors = listOf(
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.surfaceVariant
    )

    val filteredList = foodList.filter { food ->
        (selectedCategory == "Semua" || food.category == selectedCategory) &&
                food.name.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
                .padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onPrimary)
                }
                Icon(
                    Icons.Default.LocationOn, null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Lokasi Anda",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Column(modifier = Modifier.padding(start = 12.dp)) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Hai GM Lovers",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari makanan...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, "Hapus")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Column(modifier = Modifier.weight(1f).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("Kategori", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                items(categories) { (label, emoji) ->
                    val selected = selectedCategory == label
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedCategory = label }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primaryContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) { Text(emoji, fontSize = 26.sp) }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Menu Makanan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))

            if (filteredList.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Menu tidak ditemukan", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredList, key = { it.id }) { food ->
                        FoodGridCard(
                            food = food,
                            color = cardColors[(food.id - 1) % cardColors.size],
                            onClick = { onItemSelected(food) },
                            onAdd = { onQuickAdd(food) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FoodGridCard(food: FoodItem, color: Color, onClick: () -> Unit, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                food.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            FoodImage(
                food = food,
                modifier = Modifier.fillMaxWidth().height(104.dp).clip(RoundedCornerShape(20.dp)),
                emojiSize = 56
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    rupiah(food.price),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                FilledIconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, "Tambah ke keranjang", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun FoodDetailScreen(
    food: FoodItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
    onAddToCart: (String, Int) -> Unit
) {
    var selectedSize by rememberSaveable { mutableStateOf("M") }
    var quantity by rememberSaveable { mutableIntStateOf(1) }
    val unitPrice = food.price + sizeExtra(selectedSize)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
            }
            Text(
                "Detail Produk",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    "Favorit",
                    tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) { FoodImage(food, Modifier.fillMaxSize(), 110) }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf("S", "M", "L").forEach { s ->
                    val selected = s == selectedSize
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedSize = s },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            s,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    food.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(36.dp)
                        ) { Text("−", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                        Text(
                            "$quantity",
                            modifier = Modifier.padding(horizontal = 6.dp),
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier.size(36.dp)
                        ) { Icon(Icons.Default.Add, "Tambah", modifier = Modifier.size(18.dp)) }
                    }
                }
                Text(
                    rupiah(unitPrice),
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Rating", "⭐ ${food.rating}")
                StatItem("Kalori", "🔥 ${food.calories} kal")
                StatItem("Waktu", "⏱️ ${food.minutes} Min")
            }

            Spacer(Modifier.height(20.dp))
            Text("Detail", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                food.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))
            Text("Bahan-bahan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(food.ingredients) { (emoji, label) ->
                    Column(
                        modifier = Modifier
                            .width(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(emoji, fontSize = 28.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = { onAddToCart(selectedSize, quantity) },
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(56.dp)
        ) {
            Text("Add To Cart - ${rupiah(unitPrice * quantity)}", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun FavoriteScreen(
    foods: List<FoodItem>,
    onItemSelected: (FoodItem) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onBrowse: () -> Unit
) {
    if (foods.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("💚", fontSize = 64.sp)
            Spacer(Modifier.height(8.dp))
            Text("Belum ada menu favorit", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBrowse) { Text("Jelajahi Menu") }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(foods, key = { it.id }) { food ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onItemSelected(food) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) { FoodImage(food, Modifier.fillMaxSize(), 36) }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(food.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            rupiah(food.price),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    IconButton(onClick = { onToggleFavorite(food.id) }) {
                        Icon(Icons.Default.Favorite, "Hapus favorit", tint = Color(0xFFE53935))
                    }
                }
            }
        }
    }
}

@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    onIncrease: (String) -> Unit,
    onDecrease: (String) -> Unit,
    onRemove: (String) -> Unit,
    onBrowse: () -> Unit,
    onCheckout: () -> Unit
) {
    if (cartItems.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🛒", fontSize = 64.sp)
            Spacer(Modifier.height(8.dp))
            Text("Keranjang belanja kosong", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBrowse) { Text("Mulai Belanja") }
        }
        return
    }

    val subtotal = cartItems.sumOf { it.total }
    val total = subtotal + ONGKIR

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(cartItems, key = { it.key }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) { FoodImage(item.food, Modifier.fillMaxSize(), 32) }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.food.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Ukuran ${item.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                rupiah(item.total),
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(
                                    onClick = { onDecrease(item.key) },
                                    modifier = Modifier.size(30.dp)
                                ) { Text("−", fontWeight = FontWeight.Bold) }
                                Text(
                                    "${item.quantity}",
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                FilledTonalIconButton(
                                    onClick = { onIncrease(item.key) },
                                    modifier = Modifier.size(30.dp)
                                ) { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) }
                            }
                        }

                        IconButton(onClick = { onRemove(item.key) }) {
                            Icon(
                                Icons.Default.Delete, "Hapus",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        SummaryRow("Subtotal", rupiah(subtotal))
        SummaryRow("Ongkos kirim", rupiah(ONGKIR))
        Spacer(Modifier.height(4.dp))
        SummaryRow("Total", rupiah(total), bold = true)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Checkout", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (bold) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = if (bold) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (bold) MaterialTheme.colorScheme.tertiary else Color.Unspecified
        )
    }
}

@Composable
fun CheckoutScreen(cartItems: List<CartItem>, onOrderPlaced: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var saveAddress by rememberSaveable { mutableStateOf(false) }
    val paymentOptions = listOf("QRIS", "Transfer Bank", "Cash")
    var payment by rememberSaveable { mutableStateOf("Cash") }
    val banks = listOf("BCA", "Mandiri", "BNI", "BRI")
    var selectedBank by rememberSaveable { mutableStateOf<String?>(null) }

    val subtotal = cartItems.sumOf { it.total }
    val total = subtotal + ONGKIR

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Data Penerima", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nama Lengkap") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone, onValueChange = { phone = it },
                label = { Text("No. Telepon") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = address, onValueChange = { address = it },
                label = { Text("Alamat Pengiriman") },
                minLines = 2,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text("Catatan (opsional)") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { saveAddress = !saveAddress }
            ) {
                Checkbox(checked = saveAddress, onCheckedChange = { saveAddress = it })
                Text("Simpan alamat ini")
            }

            Text("Metode Pembayaran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            paymentOptions.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { payment = option }
                ) {
                    RadioButton(selected = payment == option, onClick = { payment = option })
                    Text(option)
                }
            }

            if (payment == "QRIS") {
                QrisCard(total = total)
            }
            if (payment == "Transfer Bank") {
                BankCard(
                    banks = banks,
                    selectedBank = selectedBank,
                    onSelect = { selectedBank = it },
                    total = total
                )
            }
            if (payment == "Cash") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        "Bayar tunai ke kurir saat pesanan tiba sebesar ${rupiah(total)}.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Ringkasan Pesanan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    cartItems.forEach {
                        SummaryRow("${it.quantity}x ${it.food.name} (${it.size})", rupiah(it.total))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    SummaryRow("Ongkos kirim", rupiah(ONGKIR))
                    SummaryRow("Total", rupiah(total), bold = true)
                }
            }
        }

        Button(
            onClick = { onOrderPlaced(name) },
            enabled = name.isNotBlank() && phone.isNotBlank() && address.isNotBlank() && cartItems.isNotEmpty() &&
                    (payment != "Transfer Bank" || selectedBank != null),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
                .height(56.dp)
        ) {
            Text("Buat Pesanan", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ThemeScreen(isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Pengaturan Tema", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Mode Gelap", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (isDarkTheme) "Dark Mode aktif" else "Light Mode aktif",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(checked = isDarkTheme, onCheckedChange = onThemeChange)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { onThemeChange(false) }
                ) {
                    RadioButton(selected = !isDarkTheme, onClick = { onThemeChange(false) })
                    Text("Light Mode (Terang)")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { onThemeChange(true) }
                ) {
                    RadioButton(selected = isDarkTheme, onClick = { onThemeChange(true) })
                    Text("Dark Mode (Gelap)")
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.surfaceVariant
            ).forEach { color ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun QrisCard(total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Scan QRIS untuk membayar",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(14.dp)
            ) {
                QrDummy(seed = total, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.height(12.dp))
            Text(
                rupiah(total),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
            Text(
                "QR ini hanya contoh (dummy)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QrDummy(seed: Int, modifier: Modifier = Modifier) {
    val n = 25
    val cells = remember(seed) {
        val rnd = Random(seed)
        Array(n) { BooleanArray(n) { rnd.nextBoolean() } }
    }
    Canvas(modifier = modifier) {
        val cell = size.width / n
        for (r in 0 until n) {
            for (c in 0 until n) {
                val inFinder = (r < 7 && c < 7) || (r < 7 && c >= n - 7) || (r >= n - 7 && c < 7)
                val on = if (inFinder) {
                    val rr = if (r >= n - 7) r - (n - 7) else r
                    val cc = if (c >= n - 7) c - (n - 7) else c
                    maxOf(abs(rr - 3), abs(cc - 3)) != 2
                } else {
                    cells[r][c]
                }
                if (on) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(c * cell, r * cell),
                        size = Size(cell + 0.5f, cell + 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun BankCard(
    banks: List<String>,
    selectedBank: String?,
    onSelect: (String) -> Unit,
    total: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Pilih Bank",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            banks.forEach { bank ->
                val selected = bank == selectedBank
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { onSelect(bank) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            bank.take(3).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Bank $bank",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                    RadioButton(selected = selected, onClick = { onSelect(bank) })
                }
            }

            if (selectedBank != null) {
                val prefix = when (selectedBank) {
                    "BCA" -> "39810"
                    "Mandiri" -> "88608"
                    "BNI" -> "88480"
                    else -> "26215"
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Nomor Virtual Account",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "$prefix 0812 3456",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Total transfer: ${rupiah(total)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Nomor ini hanya contoh (dummy)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FoodImage(food: FoodItem, modifier: Modifier = Modifier, emojiSize: Int = 40) {
    val context = LocalContext.current
    val resId = remember(food.imageName) {
        context.resources.getIdentifier(food.imageName, "drawable", context.packageName)
    }
    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = food.name,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(food.emoji, fontSize = emojiSize.sp)
        }
    }
}

@Composable
fun BrandLogo(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val resId = remember {
        context.resources.getIdentifier("logo_bakmi_gm", "drawable", context.packageName)
    }
    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = "Logo Bakmi GM",
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🍜", fontSize = 64.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Bakmi GM",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}