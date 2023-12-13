import { twMerge } from "tailwind-merge";
import { buttonVariants } from "../components/Button";
import { Card, CardContent, CardFooter, CardHeader } from "../components/Card";
import { Input } from "../components/Input";
import { For, createSignal } from "solid-js";
import { useNavigate } from "@solidjs/router";
import { Label } from "../components/Label";

function Chat() {
	const [askingInput, setAskingInput] = createSignal(false);
	const [error, setError] = createSignal("");
	const navigate = useNavigate();
	const [name, setName] = createSignal("");
	const [username, setUsername] = createSignal("");
	const [conversations, setConversations] = createSignal<{ [key: string]: string }>({});
	const [selectedConversation, setSelectedConversation] = createSignal<{
		id?: string;
		messages?: [{ from: string; content: string; result?: string }];
	}>({});

	let conversationEmail: HTMLInputElement;
	let message: HTMLInputElement;

	let socket: WebSocket;

	fetch("/auth", {
		method: "GET",
		credentials: "include",
	})
		.then((res) => res.json())
		.then((res) => {
			socket = new WebSocket("ws://152.70.142.191" + res.path);
			socket.onmessage = (ev: MessageEvent) => {
				let data = JSON.parse(ev.data);
				switch (data.type) {
					case "userData":
						setName(data.name);
						setUsername(data.username);
						setConversations(data.conversations);
						break;
					case "conversations":
						if (data.error) {
							setError(data.error);
						} else {
							setConversations(data.conversations);
							setAskingInput(false);
						}
						break;
					case "conversation":
						console.log(data);
						if (data.error) {
							setError(data.error);
						} else {
							if (data.id == selectedConversation().id) {
								setSelectedConversation({
									id: data.id,
									messages: data.messages,
								});
							}
						}
				}
			};
		})
		.catch((res) => {
			console.log(res);
			navigate("/login");
		});

	function addConversation(e: Event) {
		e.preventDefault();
		console.log("adding convo");
		socket.send(
			JSON.stringify({
				type: "addConversation",
				otherUser: conversationEmail.value,
			})
		);
	}

	function selectConversation(id: string) {
		setSelectedConversation({ id: id });
		socket.send(
			JSON.stringify({
				type: "getConversation",
				conversation: id,
			})
		);
	}

	function sendMessage(e: Event) {
		e.preventDefault();

		if (message.value == "") return;

		socket.send(
			JSON.stringify({
				type: "message",
				conversation: selectedConversation().id,
				message: message.value,
			})
		);

		message.value = "";
	}

	return (
		<>
			<form
				class={`dark transition-all duration-200 delay-75 rounded-lg border bg-card text-card-foreground shadow-sm z-10 ${
					askingInput() ? "opacity-100 pointer-events-auto" : "opacity-0 pointer-events-none"
				} absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-1/3`}
				onSubmit={addConversation}
			>
				<CardHeader class="pb-2">
					<div class="flex flex-row justify-between items-center">
						<h3 class="text-xl font-semibold tracking-tight text-primary">Input Username</h3>
						<button
							type="button"
							class="w-15 h-15 p-3"
							onclick={(e) => {
								e.preventDefault();
								setAskingInput(false);
							}}
						>
							<svg
								width="15"
								height="15"
								viewBox="0 0 15 15"
								fill="none"
								xmlns="http://www.w3.org/2000/svg"
							>
								<path
									d="M11.7816 4.03157C12.0062 3.80702 12.0062 3.44295 11.7816 3.2184C11.5571 2.99385 11.193 2.99385 10.9685 3.2184L7.50005 6.68682L4.03164 3.2184C3.80708 2.99385 3.44301 2.99385 3.21846 3.2184C2.99391 3.44295 2.99391 3.80702 3.21846 4.03157L6.68688 7.49999L3.21846 10.9684C2.99391 11.193 2.99391 11.557 3.21846 11.7816C3.44301 12.0061 3.80708 12.0061 4.03164 11.7816L7.50005 8.31316L10.9685 11.7816C11.193 12.0061 11.5571 12.0061 11.7816 11.7816C12.0062 11.557 12.0062 11.193 11.7816 10.9684L8.31322 7.49999L11.7816 4.03157Z"
									fill="currentColor"
									fill-rule="evenodd"
									clip-rule="evenodd"
								></path>
							</svg>
						</button>
					</div>
				</CardHeader>
				<CardContent class="pb-4">
					<Input type="text" ref={conversationEmail!}></Input>
				</CardContent>
				<CardFooter class="flex flex-col">
					<button type="submit" class={twMerge(buttonVariants({ size: "sm" }))}>
						Submit
					</button>
					{error() != "" ? <Label class="text-red-500 my-3">{error()}</Label> : <></>}
				</CardFooter>
			</form>
			<div
				class={`transition-all duration-200 delay-75 ${
					askingInput() ? "opacity-80 pointer-events-auto" : "opacity-0 pointer-events-none"
				} absolute w-screen h-screen bg-black top-0 left-0`}
			></div>

			<div class="dark text-primary bg-background w-screen h-screen py-8 px-12 flex flex-col space-y-6">
				<div class="flex items-center justify-between h-[15vh]">
					<div>
						<h2 class="text-2xl font-bold tracking-tight">Welcome, {name()}!</h2>
						<p class="text-muted-foreground">Here&apos;s your conversations.</p>
					</div>
					<div class="flex items-center space-x-4">
						<button
							class={twMerge(buttonVariants({ variant: "destructive", size: "sm" }), "")}
							onClick={() => {
								socket.close();
								navigate("/");
							}}
						>
							Log Out
						</button>
						<div class="flex h-full w-full items-center justify-center rounded-full bg-muted px-4 py-3">
							SI
						</div>
					</div>
				</div>
				<Card class="flex-1 grid grid-cols-8 border-ring h-[85vh]">
					<div class="col-span-2 rounded-l border-r ">
						<div class="w-full h-full flex flex-col divide-y">
							<div class="w-full h-12 items-center justify-center flex flex-col">
								<button
									class={twMerge(buttonVariants({ size: "sm", variant: "secondary" }))}
									onclick={() => setAskingInput(true)}
								>
									<svg
										class="w-4 h-4 mr-2"
										viewBox="0 0 15 15"
										fill="none"
										xmlns="http://www.w3.org/2000/svg"
									>
										<path
											d="M8 2.75C8 2.47386 7.77614 2.25 7.5 2.25C7.22386 2.25 7 2.47386 7 2.75V7H2.75C2.47386 7 2.25 7.22386 2.25 7.5C2.25 7.77614 2.47386 8 2.75 8H7V12.25C7 12.5261 7.22386 12.75 7.5 12.75C7.77614 12.75 8 12.5261 8 12.25V8H12.25C12.5261 8 12.75 7.77614 12.75 7.5C12.75 7.22386 12.5261 7 12.25 7H8V2.75Z"
											fill="currentColor"
											fill-rule="evenodd"
											clip-rule="evenodd"
										></path>
									</svg>
									Add Conversation
								</button>
							</div>
							{Object.keys(conversations()).length == 0 ? (
								<div class="flex-1 flex flex-col items-center justify-center">
									<h1>No Previous Conversations Found</h1>
								</div>
							) : (
								<div class="flex flex-col">
									<For each={Object.keys(conversations())}>
										{(conversation) => (
											<div
												class={`flex flex-row p-4 items-center space-x-2 hover:bg-secondary ${
													selectedConversation().id == conversation ? "bg-secondary" : ""
												}`}
												onClick={() => selectConversation(conversation)}
											>
												<div class="flex w-16 items-center justify-center rounded-full bg-accent p-4 mr-2">
													{conversations()[conversation].substring(0, 2) ?? "NA"}
												</div>
												<div class="w-full text-lg font-semibold tracking-tight text-primary">
													{conversations()[conversation]}
												</div>
											</div>
										)}
									</For>
								</div>
							)}
						</div>
					</div>
					<div class="col-span-6 flex flex-col-reverse p-4 overflow-hidden">
						<form class="flex w-full items-center space-x-2 mt-4" onSubmit={sendMessage}>
							<Input class="flex-1" placeholder="Send a message" type="text" ref={message!} />
							<button class={twMerge(buttonVariants(), "w-14")} type="submit">
								<svg
									width="15"
									height="15"
									viewBox="0 0 15 15"
									fill="none"
									xmlns="http://www.w3.org/2000/svg"
								>
									<path
										d="M1.20308 1.04312C1.00481 0.954998 0.772341 1.0048 0.627577 1.16641C0.482813 1.32802 0.458794 1.56455 0.568117 1.75196L3.92115 7.50002L0.568117 13.2481C0.458794 13.4355 0.482813 13.672 0.627577 13.8336C0.772341 13.9952 1.00481 14.045 1.20308 13.9569L14.7031 7.95693C14.8836 7.87668 15 7.69762 15 7.50002C15 7.30243 14.8836 7.12337 14.7031 7.04312L1.20308 1.04312ZM4.84553 7.10002L2.21234 2.586L13.2689 7.50002L2.21234 12.414L4.84552 7.90002H9C9.22092 7.90002 9.4 7.72094 9.4 7.50002C9.4 7.27911 9.22092 7.10002 9 7.10002H4.84553Z"
										fill="currentColor"
										fill-rule="evenodd"
										clip-rule="evenodd"
									></path>
								</svg>
							</button>
						</form>
						<div class="flex flex-col-reverse max-h-full h-full transition-all duration-300 overflow-y-auto pr-2">
							{selectedConversation().id == null ? (
								<div class="flex-1 flex justify-center items-center text-3xl font-medium">
									No Conversation Selected
								</div>
							) : selectedConversation().messages != null &&
							  selectedConversation().messages!.length > 0 ? (
								<For each={selectedConversation().messages}>
									{(message) => (
										<div
											class={`${
												message.from == username()
													? "self-end bg-blue-500 text-right"
													: "self-start bg-slate-500 text-left"
											} ${
												message.result != undefined ? "rounded-2xl" : "rounded-full"
											} px-3 py-1 mt-1`}
										>
											{message.result != undefined ? (
												<div>
													<div class="text-sm font-light">{message.content}</div>
													<div class="text-lg">= {message.result}</div>
												</div>
											) : (
												message.content
											)}
										</div>
									)}
								</For>
							) : (
								<div class="self-center text-sm font-light">No Messages So Far</div>
							)}
						</div>
					</div>
				</Card>
			</div>
		</>
	);
}

export default Chat;
