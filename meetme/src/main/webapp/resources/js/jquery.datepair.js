(function($) {

	if(!$) {
		return;
	}

	$.fn.datepair = function(option) {
		var out;
		this.each(function() {
			var $this = $(this);
			var data = $this.data('datepair');
			var options = typeof option === 'object' && option;

			if (!data) {
				data = new Datepair(this, options);
				$this.data('datepair', data);
			}

			if (typeof option === 'string') {
				out = data[option]();
			}
		});

		return out || this;
	};

	$('[data-datepair]').each(function() {
		var $this = $(this);
		$this.datepair($this.data());
	});

}(window.Zepto || window.jQuery));
