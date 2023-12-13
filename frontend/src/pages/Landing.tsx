import { Button } from "../components/Button";

function Landing() {
	return (
		<div class="dark bg-background text-primary  w-screen h-screen flex flex-col">
			<header class="sticky top-0 z-50 w-full border-b bg-background/95">
				<div class="flex flex-row h-14 items-center px-12 md:px-32 justify-between">
					<div class="flex flex-row items-center">
						<Button href="/" class="text-xl font-semibold" variant="link">
							MathChat
						</Button>
						<Button
							class="text-foreground/60 hover:text-foreground/80"
							target="_blank"
							href="https://docs.google.com"
							variant="link"
						>
							Documentation
						</Button>
						<Button
							class="text-foreground/60 hover:text-foreground/80"
							target="_blank"
							href="https://github.com/hackedXD"
							variant="link"
						>
							Github
						</Button>
					</div>
					<div class="flex flex-row items-center space-x-4">
						<Button href="/login" variant="outline">
							Log In
						</Button>
					</div>
				</div>
			</header>
			<div class="flex-1 flex flex-col itmes-center justify-center">
				<div class="container flex flex-col items-center space-y-8">
					<h1 class="scroll-m-20 text-6xl font-extrabold tracking-tight text-center">
						Integrate math into your conversations seamlessly
					</h1>
					<Button href="/signup" size="lg">
						Get Started
					</Button>
				</div>
			</div>
		</div>
	);
}

export default Landing;
