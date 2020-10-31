window.onscroll = function() {scrollFunction()};

function scrollFunction() {
	mybutton = document.getElementById("backToTopBtn");
  if (document.body.scrollTop > 20 || document.documentElement.scrollTop > 20) {
    mybutton.style.display = "block";
  } else {
    mybutton.style.display = "none";
  }
}

function topFunction() {
  document.body.scrollTop = 0; // For Safari
  document.documentElement.scrollTop = 0; // For Chrome, Firefox, IE and Opera
}

function openNav(id) {
  if (document.getElementById(id).style.width != "0px" && document.getElementById(id).style.width != 0)
	closeNav(id);
  else
    document.getElementById(id).style.width = "400px";
}

function closeNav(id) {
  document.getElementById(id).style.width = "0";
}