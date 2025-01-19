package com.example.fooddeliveryapp.ui.screen.home_components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddeliveryapp.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodaysMenuSection() {
    val cards = listOf(
        CardData("Free Donut!",
            "For orders over $20",
            R.drawable.img_donut,
            Color(0xFF79be95)
        ),
        CardData("Cool Drinks!",
            "Refreshing all day",
            R.drawable.img_drinks,
            Color(0xFF77AADD)
        ),
        CardData("Amazing Cakes!",
            "Enhance your flavor",
            R.drawable.img_cake,
            Color(0xFF8BC34A)
        )
    )
    var currentCardIndex by remember { mutableStateOf(0) }
    val offsetAnimation1 = remember { Animatable(0f) }
    val offsetAnimation2 = remember { Animatable(400f) }

    LaunchedEffect(currentCardIndex) {
        while (true) {
            delay(3000)
            offsetAnimation1.animateTo(
                targetValue = -400f, // Slide left
                animationSpec = tween(durationMillis = 1000)
            )
            offsetAnimation2.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1000),
            )

            currentCardIndex = (currentCardIndex + 1) % cards.size
            offsetAnimation1.snapTo(0f)
            offsetAnimation2.snapTo(400f)

        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Today's Menu",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp), // Adjust the height as needed
            contentAlignment = Alignment.Center
        ) {
            /*// Display the current card and the next card to create the loop effect
            val currentCard = cards[currentCardIndex]
            val nextCard = cards[(currentCardIndex + 1) % cards.size]

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp) // Adding space between the cards
            ) {*/
            FeaturedCard( title = cards[currentCardIndex].title,
                subtitle = cards[currentCardIndex].subtitle,
                imageResId = cards[currentCardIndex].imageResId,
                backgroundColor = cards[currentCardIndex].backgroundColor,
                offset = offsetAnimation1.value )

                Spacer(modifier = Modifier.width(16.dp)) // Adding space between cards

            FeaturedCard( title = cards[(currentCardIndex + 1) % cards.size].title,
                subtitle = cards[(currentCardIndex + 1) % cards.size].subtitle,
                imageResId = cards[(currentCardIndex + 1) % cards.size].imageResId,
                backgroundColor = cards[(currentCardIndex + 1) % cards.size].backgroundColor,
                offset = offsetAnimation2.value
            ) }
        }
    }

data class CardData(
    val title: String,
    val subtitle: String,
    val imageResId: Int,
    val backgroundColor: Color
)

@Composable
fun FeaturedCard(
    title: String,
    subtitle: String,
    imageResId: Int,
    backgroundColor: Color,
    offset: Float
) {
    Box(
        modifier = Modifier
            .width(360.dp)
            .height(130.dp)
            .offset(x = offset.dp)
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .width(300.dp)
                .height(120.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }
            }
        }

        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterEnd)
                .offset(x = (-5).dp)
                .offset(y = (-30).dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )
    }
}
