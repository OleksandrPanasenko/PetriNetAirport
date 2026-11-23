from scipy.optimize import brentq
from scipy.stats import chi2, norm
from scipy.stats import norm
import matplotlib.pyplot as plt
import numpy as np
from scipy.optimize import root_scalar
from scipy.stats import norm, gamma
import scipy.stats as stats
from sklearn.mixture import GaussianMixture

def sample_blackbox_cdf_exact(cdf_func, u_val, bracket=(-100.0, 100.0)):
    """Solves F(x) - u = 0 for a given uniform random number u."""
    res = root_scalar(
        lambda x: cdf_func(x) - u_val,
        bracket=bracket,
        method="brentq"
    )
    return res.root

def make_empirical_cdf(ref_sample):
    x_sorted = np.sort(np.asarray(ref_sample, dtype=float))
    eps = np.arange(len(x_sorted)) * 1e-9
    x_unique = x_sorted + eps
    y_probs = np.linspace(0.0, 1.0, len(x_unique))
    
    def cdf(x):
        return float(np.interp(x, x_unique, y_probs, left=0.0, right=1.0))
    
    return cdf

from scipy.stats import uniform, norm
# Критерій узгодженості Пірсона (хі-квадрат) для неперервного розподілу
# Повертає наближене p-значення:
# p < 0.05 — відхиляємо задану гіпотезу
# p >= 0.05 — недостатньо підстав її відхилити
def chi_squared_check(
    data,
    cdf=lambda x: norm.cdf(x, loc=0, scale=1),
    bins=10,
    param_count=0,
    mode="string", #dict, p, string
    treshold=0.05
):
    data = np.asarray(data, dtype=float)

    n = data.size

    def checked_cdf(x):
        value = float(cdf(x))
        if not np.isfinite(value) or not 0.0 <= value <= 1.0:
            raise ValueError('CDF має повертати скінченне значення від 0 до 1')
        return value

    # Знаходимо діапазон для внутрішніх теоретичних квантилів (першого і останнього).
    # Межі пошуку не залежать від вибірки.
    probabilities = np.arange(1, bins, dtype=float) / bins
    left, right = -1.0, 1.0

    for _ in range(1024):
        if (
            checked_cdf(left) <= probabilities[0]
            and checked_cdf(right) >= probabilities[-1]
        ):
            break

        if checked_cdf(left) > probabilities[0]:
            left *= 2.0
        if checked_cdf(right) < probabilities[-1]:
            right *= 2.0

        if not np.isfinite(left) or not np.isfinite(right):
            raise ValueError('Не вдалося знайти діапазон квантилів')
    else:
        raise ValueError('Не вдалося знайти діапазон квантилів')

    inner_edges = np.array(
        [
            brentq(
                lambda x: checked_cdf(x) - probability,
                left,
                right,
            )
            for probability in probabilities
        ]
    )

    if np.any(np.diff(inner_edges) <= 0):
        raise ValueError('Не вдалося отримати різні межі інтервалів')

    # Хвости обов’язково входять до першого й останнього інтервалів.
    edges = np.concatenate(([-np.inf], inner_edges, [np.inf]))
    observed, _ = np.histogram(data, bins=edges)

    cdf_edges = np.array([0.0] + [checked_cdf(x) for x in inner_edges] + [1.0])
    expected = n * np.diff(cdf_edges)

    if np.any(expected <= 0):
        raise ValueError('Некоректні ймовірності інтервалів: перевірте CDF')

    # Об’єднуємо сусідні інтервали з очікуваною частотою < 5.
    merged_observed = []
    merged_expected = []
    i = 0

    while i < len(expected):
        cum_obs = observed[i]
        cum_exp = expected[i]
        i += 1

        while cum_exp < 5 and i < len(expected):
            cum_obs += observed[i]
            cum_exp += expected[i]
            i += 1

        merged_observed.append(cum_obs)
        merged_expected.append(cum_exp)

    if len(merged_expected) > 1 and merged_expected[-1] < 5:
        merged_observed[-2] += merged_observed.pop()
        merged_expected[-2] += merged_expected.pop()

    observed = np.asarray(merged_observed)
    expected = np.asarray(merged_expected)


    dof = len(expected) - 1 - param_count
    if dof <= 0 or np.any(expected < 5):
        raise ValueError('Недостатньо інтервалів з очікуваною частотою не менше 5')

    chi2_stat = np.sum((observed - expected) ** 2 / expected)
    p=float(chi2.sf(chi2_stat, dof))
    if mode=="p":
        return p
    elif mode=="dict":
        return {"chi": chi2_stat, "chi_treshold": chi2.isf(treshold, dof), 'p': p, 'p_treshold': treshold, 'dof': dof}
    elif mode=="string":
        return f"dof: {dof}, chi2_stat: {chi2_stat}, chi2_treshold: {chi2.isf(treshold, dof)}\n{p}{'>=' if p >= treshold else '<'}{treshold}: {'не відхиляємо' if p >= treshold else 'відхиляємо'} гіпотезу\n"

def draw_as_hists(samples, label="Розподіл", N=50, draw_norm=False, ax=None):
    
    if ax is None:
        plt.figure(figsize=(12, 9))    
    counts, bins, _ = ax.hist(samples, bins=N, density=True, alpha=0.6, color='blue')

    x = np.linspace(min(bins), max(bins), 10000)
    if draw_norm:
        pdf = norm.pdf(x, loc=0.5, scale=0.15)
        ax.plot(x, pdf, 'r-', linewidth=2, label='Теоретична щільність розподілу')
    
    ax.set_title(f'Гістограма розподілу {label} із {N} інтервалами (ширина інтервалу {1/N:.2f})')
    ax.set_xlabel('Величина')
    ax.set_ylabel('Щільність')
    if draw_norm:
        ax.legend()
    ax.grid(True)
    
    return ax

def draw_as_hists_compared(samples_estimate, samples_true, label="Розподіл", N=50, draw_norm=False, ax=None):
    if ax is None:
        fig, ax = plt.subplots(figsize=(12, 9))
    ax.hist(samples_true, bins=N, density=True, alpha=0.6,  color='blue',linewidth=0)
    counts, bins, _ = ax.hist(samples_estimate, bins=N, density=True, alpha=0.6, color='red',linewidth=0)

    x1 = np.linspace(min(bins), max(bins), 10000)
    if draw_norm:
        pdf = norm.pdf(x1, loc=0.5, scale=0.15)
        ax.plot(x1, pdf, 'r-', linewidth=2, label='Теоретична щільність розподілу')


    ax.set_title(f'Гістограма розподілу {label} із {N} інтервалами \n(ширина інтервалу {1/N:.2f})', fontsize=9)
    ax.set_xlabel('Величина', fontsize=9)
    ax.set_ylabel('Щільність', fontsize=9)
    if draw_norm:
        ax.legend()
    ax.grid(True)
    return  ax

def distribution_analysis(array,hist_bins=200):
    fig,ax=plt.subplots(3,2,figsize=(8,12))
    if(ax is None):
        raise ValueError("Is null!!!")

    sample_3=array
    draw_as_hists(sample_3, N=hist_bins, draw_norm=False, label="Експеримент",ax=ax[0,0])


    #Gamma distribution
    a_fit, loc_fit, scale_fit = stats.gamma.fit(sample_3)

    k_erlang=2

    print(f"Параметри розподілу Ерланга: k={k_erlang}, loc={loc_fit}, scale={scale_fit}")

    distribution_erl = lambda x: gamma.cdf(x, a=k_erlang, scale=scale_fit,loc=loc_fit)
    sample_erl=gamma.rvs(a=k_erlang, scale=scale_fit, loc=loc_fit, size=len(sample_3))
    draw_as_hists_compared(samples_estimate=sample_erl, samples_true=sample_3, N=hist_bins,label="Гамма",draw_norm=False,ax=ax[0,1])
    cdf = lambda x: distribution_erl(x, k_erlang, scale_fit)
    print(f"Подібність найбільш відповідного розподілу Ерланга: {chi_squared_check(sample_3,distribution_erl)}\n")

    #Normal distribution
    mean_normal=np.mean(sample_3)
    std_normal=np.std(sample_3)
    print(f"Параметри Нормального розподілу: mean={mean_normal}, std={std_normal}")
    distribution_norm = lambda x: norm.cdf(x, loc=mean_normal, scale=std_normal)
    print(f"Подібність розподілу Нормального: {chi_squared_check(sample_3,distribution_norm)}\n")
    draw_as_hists_compared(samples_estimate=norm.rvs(loc=mean_normal, scale=std_normal, size=len(sample_3)), samples_true=sample_3, N=hist_bins, label="Нормальний", draw_norm=False,ax=ax[1,0])

    #Exponential distribution
    loc_exp, scale_exp = stats.expon.fit(sample_3, floc=0)
    print(f"Параметри Експоненційного розподілу: loc={loc_exp}, scale={scale_exp}")
    distribution_exp = lambda x: stats.expon.cdf(x, loc=loc_exp, scale=scale_exp)
    sample_exp = stats.expon.rvs(loc=loc_exp, scale=scale_exp, size=len(sample_3))
    draw_as_hists_compared(samples_estimate=sample_exp, samples_true=sample_3, N=hist_bins, label="Експоненційний", draw_norm=False,ax=ax[1,1])
    print("Chi-squared test (Exponential):", chi_squared_check(sample_3, distribution_exp), "\n")

    #Log-normal distribution
    s_fit, loc_lognorm, scale_lognorm = stats.lognorm.fit(sample_3, floc=0)
    print(f"Параметри Логнормального розподілу: s={s_fit}, loc={loc_lognorm}, scale={scale_lognorm}")
    distribution_lognorm = lambda x: stats.lognorm.cdf(x, s=s_fit, loc=loc_lognorm, scale=scale_lognorm)
    sample_lognorm = stats.lognorm.rvs(s=s_fit, loc=loc_lognorm, scale=scale_lognorm, size=len(sample_3))
    draw_as_hists_compared(samples_estimate=sample_lognorm, samples_true=sample_3, N=hist_bins, label="Логнормальний", draw_norm=False,ax=ax[2,0])
    print("Chi-squared test (Log-Normal):", chi_squared_check(sample_3, distribution_lognorm), "\n")

    #Двовершинний
    data_2d = np.array(sample_3).reshape(-1, 1)
    gmm = GaussianMixture(n_components=2, random_state=42).fit(data_2d)

    weights = gmm.weights_
    means = gmm.means_.flatten()
    stds = np.sqrt(gmm.covariances_.flatten())

    print(f"Двовершинний розподіл (Суміш 2-х нормальних):")
    print(f"  Компонента 1: w={weights[0]:.2f}, mean={means[0]:.4f}, std={stds[0]:.4f}")
    print(f"  Компонента 2: w={weights[1]:.2f}, mean={means[1]:.4f}, std={stds[1]:.4f}")
    distribution_bimodal = lambda x: (
        weights[0] * stats.norm.cdf(x, loc=means[0], scale=stds[0]) +
        weights[1] * stats.norm.cdf(x, loc=means[1], scale=stds[1])
    )
    sample_bimodal = np.where(np.random.rand(len(sample_3)) < weights[0], stats.norm.rvs(loc=means[0], scale=stds[0], size=len(sample_3)), stats.norm.rvs(loc=means[1], scale=stds[1], size=len(sample_3)))
    draw_as_hists_compared(samples_estimate=sample_bimodal, samples_true=sample_3, N=hist_bins, label="Двовершинний", draw_norm=False,ax=ax[2,1])
    # Chi-squared test
    print("Chi-squared test (Bimodal Gaussian):", chi_squared_check(sample_3, distribution_bimodal))
    plt.tight_layout()
    plt.show()
