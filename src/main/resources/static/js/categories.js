const categoryModal = document.getElementById('category-modal');
const openCategoryModal = document.getElementById('open-category-modal');
const closeCategoryModal = document.getElementById('close-category-modal');

function showCategoryModal() {
    if (!categoryModal) return;
    categoryModal.classList.remove('hidden');
    categoryModal.classList.add('flex');
}

function hideCategoryModal() {
    if (!categoryModal) return;
    categoryModal.classList.remove('flex');
    categoryModal.classList.add('hidden');
}

if (openCategoryModal) {
    openCategoryModal.addEventListener('click', showCategoryModal);
}

if (closeCategoryModal) {
    closeCategoryModal.addEventListener('click', hideCategoryModal);
}

if (categoryModal) {
    categoryModal.addEventListener('click', (event) => {
        if (event.target === categoryModal) {
            hideCategoryModal();
        }
    });

    if (categoryModal.dataset.editOpen === 'true') {
        showCategoryModal();
    }
}